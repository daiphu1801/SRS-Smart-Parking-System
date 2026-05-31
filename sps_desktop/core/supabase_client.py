import asyncio
import threading
import os
from typing import Callable, List
from supabase import create_async_client
from dotenv import load_dotenv

load_dotenv()

SUPABASE_URL = os.environ.get("SUPABASE_URL", "")
SUPABASE_ANON_KEY = os.environ.get("SUPABASE_ANON_KEY", "")

_callbacks: List[Callable[[dict], None]] = []
_loop = None

def register_realtime_callback(callback: Callable[[dict], None]):
    """Registers a callback to receive realtime payloads."""
    if callback not in _callbacks:
        _callbacks.append(callback)

def unregister_realtime_callback(callback: Callable[[dict], None]):
    """Unregisters a previously added callback."""
    if callback in _callbacks:
        _callbacks.remove(callback)

def _dispatch_payload(payload):
    # Convert payload to dict if it's a Pydantic model
    if hasattr(payload, "model_dump"):
        payload = payload.model_dump()
    elif hasattr(payload, "dict"):
        payload = payload.dict()
    elif not isinstance(payload, dict):
        try:
            payload = dict(payload)
        except Exception:
            payload = {"raw": str(payload), "type": "UNKNOWN"}

    for cb in _callbacks:
        try:
            cb(payload)
        except Exception as e:
            print(f"[Supabase Realtime] Error in callback: {e}")

async def _listen_loop(jwt_token: str = None):
    supabase = await create_async_client(SUPABASE_URL, SUPABASE_ANON_KEY)
    
    if jwt_token:
        try:
            await supabase.realtime.set_auth(jwt_token)
            print("[Supabase Realtime] Authenticated channel with JWT")
        except Exception as e:
            print(f"[Supabase Realtime] Failed to set auth: {e}")
            
    # Listen to changes on the notifications table
    channel = supabase.channel("public:notifications")
    
    # Subscribe to all events (* = INSERT, UPDATE, DELETE)
    channel.on_postgres_changes(
        event="*",
        schema="public",
        table="notifications",
        callback=_dispatch_payload
    )
    
    try:
        await channel.subscribe()
        print("[Supabase Realtime] Subscribed to public:notifications channel.")
        
        # Keep the connection alive
        while True:
            await asyncio.sleep(1)
            
    except asyncio.CancelledError:
        print("[Supabase Realtime] Listener cancelled.")
    except Exception as e:
        print(f"[Supabase Realtime] Fatal error: {e}")

def _thread_target(jwt_token: str = None):
    global _loop
    _loop = asyncio.new_event_loop()
    asyncio.set_event_loop(_loop)
    try:
        _loop.run_until_complete(_listen_loop(jwt_token))
    finally:
        _loop.close()

def start_realtime_listener(jwt_token: str = None):
    """Starts the Supabase realtime listener in a background thread."""
    thread = threading.Thread(target=_thread_target, args=(jwt_token,), daemon=True, name="SupabaseRealtimeThread")
    thread.start()

def upload_image_to_storage(image_bytes: bytes, filename: str, jwt_token: str):
    """
    Synchronously uploads an image to the parking-images bucket via REST API.
    Used for IoT entry/exit check-ins.
    """
    if not SUPABASE_URL or not jwt_token:
        print("[Supabase Storage] Missing URL or token")
        return
        
    url = f"{SUPABASE_URL}/storage/v1/object/parking-images/{filename}"
    headers = {
        "Authorization": f"Bearer {jwt_token}",
        "Content-Type": "image/jpeg"
    }
    
    try:
        import requests
        resp = requests.post(url, headers=headers, data=image_bytes)
        if not resp.ok:
            print(f"[Supabase Storage] Failed to upload {filename}. 400 Bad Request? Text: {resp.text}")
        resp.raise_for_status()
        print(f"[Supabase Storage] Uploaded {filename} successfully.")
    except Exception as e:
        print(f"[Supabase Storage] Error Exception: {e}")

