"""
api_client.py — HTTP client for Smart Parking System Spring Boot backend.
All calls map directly to the documented Java REST API endpoints.
"""

import requests
from typing import Optional, Any

BASE_URL = "http://localhost:8080/api/v1"

# Shared session-level auth token (set after login)
_token: Optional[str] = None
_account_type: Optional[str] = None
_account_id: Optional[int] = None
_permissions: list[str] = []
_notification_callback = None


def set_notification_callback(callback):
    global _notification_callback
    _notification_callback = callback


def _notify(message: str, is_error: bool = False):
    if _notification_callback:
        _notification_callback(message, is_error)


def set_token(token: str, account_type: str, account_id: int, permissions: list[str] = None):
    global _token, _account_type, _account_id, _permissions
    _token = token
    _account_type = account_type
    _account_id = account_id
    _permissions = permissions or []


def clear_token():
    global _token, _account_type, _account_id, _permissions
    _token = None
    _account_type = None
    _account_id = None
    _permissions = []


def get_account_type() -> Optional[str]:
    return _account_type


def get_account_id() -> Optional[int]:
    return _account_id


def get_permissions() -> list[str]:
    return list(_permissions)


def _headers() -> dict:
    if _token:
        return {"Authorization": f"Bearer {_token}", "Content-Type": "application/json"}
    return {"Content-Type": "application/json"}


def _snake_to_camel(name: str) -> str:
    parts = name.split("_")
    return parts[0] + "".join(p[:1].upper() + p[1:] for p in parts[1:])


def _camel_to_snake(name: str) -> str:
    out = []
    for ch in name:
        if ch.isupper() and out:
            out.append("_")
        out.append(ch.lower())
    return "".join(out)


def _camelize(value: Any) -> Any:
    if isinstance(value, dict):
        return {
            _snake_to_camel(k) if isinstance(k, str) else k: _camelize(v)
            for k, v in value.items()
            if v is not None
        }
    if isinstance(value, list):
        return [_camelize(v) for v in value]
    return value


def _add_snake_aliases(value: Any) -> Any:
    if isinstance(value, list):
        return [_add_snake_aliases(v) for v in value]
    if isinstance(value, dict):
        out = {k: _add_snake_aliases(v) for k, v in value.items()}
        for k, v in list(out.items()):
            if isinstance(k, str):
                snake = _camel_to_snake(k)
                if snake != k and snake not in out:
                    out[snake] = v
        return out
    return value


def _clean(value: dict | None, *, camelize: bool = True) -> dict | None:
    if value is None:
        return None
    return _camelize(value) if camelize else {k: v for k, v in value.items() if v is not None}


def _check_status(resp: requests.Response):
    """
    Checks HTTP status. If an error occurred, attempts to extract the 
    custom error message from Java's ApiResponse wrapper before raising.
    """
    if resp.status_code >= 400:
        try:
            body = resp.json()
            if isinstance(body, dict) and "message" in body:
                raise Exception(body["message"])
        except ValueError:
            pass
        resp.raise_for_status()


def _decode_response(resp: requests.Response) -> dict:
    if not resp.content:
        return {}
    try:
        return _add_snake_aliases(resp.json())
    except ValueError:
        return {"data": resp.text}


def _wrap_data(data: Any) -> dict:
    if isinstance(data, dict) and ("data" in data or "success" in data):
        return data
    return {"data": data}


def _get(path: str, params: dict = None, *, camelize_params: bool = True) -> dict:
    """GET with auth header. Returns parsed JSON or raises on error."""
    try:
        resp = requests.get(f"{BASE_URL}{path}", headers=_headers(), params=_clean(params, camelize=camelize_params), timeout=10)
        _check_status(resp)
        return _decode_response(resp)
    except Exception as e:
        _notify(str(e), is_error=True)
        raise e


def _post(path: str, body: dict = None, files=None, params: dict = None, *, camelize_body: bool = True) -> dict:
    try:
        if files:
            h = {"Authorization": f"Bearer {_token}"} if _token else {}
            resp = requests.post(f"{BASE_URL}{path}", headers=h, files=files, timeout=15)
        else:
            resp = requests.post(
                f"{BASE_URL}{path}",
                headers=_headers(),
                json=_clean(body, camelize=camelize_body),
                params=_clean(params),
                timeout=10,
            )
        _check_status(resp)
        data = _decode_response(resp)
        msg = data.get("message") if isinstance(data, dict) else None
        _notify(msg or "Thành công", is_error=False)
        return data
    except Exception as e:
        _notify(str(e), is_error=True)
        raise e


def _put(path: str, body: dict = None, *, camelize_body: bool = True) -> dict:
    try:
        resp = requests.put(f"{BASE_URL}{path}", headers=_headers(), json=_clean(body, camelize=camelize_body), timeout=10)
        _check_status(resp)
        data = _decode_response(resp)
        msg = data.get("message") if isinstance(data, dict) else None
        _notify(msg or "Thành công", is_error=False)
        return data
    except Exception as e:
        _notify(str(e), is_error=True)
        raise e


def _patch(path: str, body: dict = None, *, camelize_body: bool = True) -> dict:
    try:
        resp = requests.patch(f"{BASE_URL}{path}", headers=_headers(), json=_clean(body, camelize=camelize_body), timeout=10)
        _check_status(resp)
        data = _decode_response(resp)
        msg = data.get("message") if isinstance(data, dict) else None
        _notify(msg or "Thành công", is_error=False)
        return data
    except Exception as e:
        _notify(str(e), is_error=True)
        raise e


def _delete(path: str) -> dict:
    try:
        resp = requests.delete(f"{BASE_URL}{path}", headers=_headers(), timeout=10)
        _check_status(resp)
        data = _decode_response(resp)
        msg = data.get("message") if isinstance(data, dict) else None
        _notify(msg or "Thành công", is_error=False)
        return data
    except Exception as e:
        _notify(str(e), is_error=True)
        raise e


# ════════════════════════════════════════════════════
# PHẦN 1: AUTH
# ════════════════════════════════════════════════════

def login(username: str, password: str) -> dict:
    """POST /api/v1/auth/login"""
    return _post("/auth/login", {"username": username, "password": password})


def check_phone(phone: str) -> dict:
    """GET /api/v1/auth/check-phone/{phone}"""
    return _get(f"/auth/check-phone/{phone}")


def logout() -> dict:
    """POST /api/v1/auth/logout"""
    return _post("/auth/logout")


def get_me() -> dict:
    """GET /api/v1/auth/me"""
    return _get("/auth/me")


def forgot_password(phone: str) -> dict:
    """POST /api/v1/auth/forgot-password/{phone} — phone is a path variable"""
    return _post(f"/auth/forgot-password/{phone}")


def register(phone: str, password: str, otp_code: str = None) -> dict:
    """POST /api/v1/auth/register"""
    return _post("/auth/register", {"phone": phone, "password": password, "otp_code": otp_code}, camelize_body=False)


def send_otp(phone: str, otp_type: str) -> dict:
    """POST /api/v1/auth/send-otp"""
    return _post("/auth/send-otp", {"phone": phone, "type": otp_type})


def reset_password(phone: str, otp_code: str, new_password: str) -> dict:
    """POST /api/v1/auth/reset-password"""
    return _post("/auth/reset-password", {"phone": phone, "otp_code": otp_code, "new_password": new_password})


def change_password(old_password: str, new_password: str) -> dict:
    """POST /api/v1/auth/change-password"""
    return _post("/auth/change-password", {"old_password": old_password, "new_password": new_password})


# ════════════════════════════════════════════════════
# PHẦN 2.1: ADMIN — ACCOUNTS & ROLES
# ════════════════════════════════════════════════════

def admin_get_roles(search: str = None) -> dict:
    """GET /api/v1/admin/roles"""
    return _get("/admin/roles", params={"search": search} if search else None)


def admin_get_role(role_id: int) -> dict:
    """GET /api/v1/admin/roles/{id}"""
    return _get(f"/admin/roles/{role_id}")


def admin_get_role_functions_actions() -> dict:
    """GET /api/v1/admin/roles/functions-actions"""
    return _get("/admin/roles/functions-actions")


def admin_create_role(role_name: str, description: str = None, permissions: list[dict] = None) -> dict:
    """POST /api/v1/admin/roles"""
    return _post("/admin/roles", {"role_name": role_name, "description": description, "permissions": permissions or []})


def admin_update_role(role_id: int, role_name: str, description: str = None, permissions: list[dict] = None) -> dict:
    """PUT /api/v1/admin/roles/{id}"""
    return _put(f"/admin/roles/{role_id}", {"role_name": role_name, "description": description, "permissions": permissions or []})


def admin_delete_role(role_id: int) -> dict:
    """DELETE /api/v1/admin/roles/{id}"""
    return _delete(f"/admin/roles/{role_id}")


def admin_hard_delete_role(role_id: int) -> dict:
    """DELETE /api/v1/admin/roles/hard/{id}"""
    return _delete(f"/admin/roles/hard/{role_id}")


def admin_get_accounts(page=0, size=20, role_id=None, account_type=None, status=None,
                       search=None, username=None, is_unassigned=None) -> dict:
    """GET /api/v1/admin/accounts"""
    params = {"page": page, "size": size}
    if role_id not in (None, ""): params["role_id"] = role_id
    if account_type not in (None, ""): params["account_type"] = account_type
    if status not in (None, ""): params["status"] = status
    username = username or search
    if username: params["username"] = username
    if is_unassigned is not None: params["is_unassigned"] = is_unassigned
    return _get("/admin/accounts", params=params)


def admin_update_account(account_id: int, body: dict) -> dict:
    """PUT /api/v1/admin/accounts/{id}"""
    return _put(f"/admin/accounts/{account_id}", body)


# ════════════════════════════════════════════════════
# PHẦN 2.1: ADMIN — SYSTEM CONFIGS
# ════════════════════════════════════════════════════

def admin_get_system_configs() -> dict:
    """GET /api/v1/admin/system-configs"""
    return _get("/admin/system-configs")

def admin_update_system_config(config_id: int, config_value: str, description: str = None) -> dict:
    """PUT /api/v1/admin/system-configs/{id}"""
    body = {"configValue": config_value}
    if description is not None:
        body["description"] = description
    return _put(f"/admin/system-configs/{config_id}", body, camelize_body=False)

# ════════════════════════════════════════════════════
# PHẦN 2.2: ADMIN — EMPLOYEES
# ════════════════════════════════════════════════════

def admin_get_employees(page=0, size=20, search=None, full_name=None, phone=None, is_online=None) -> dict:
    """GET /api/v1/admin/employees"""
    params = {"page": page, "size": size}
    full_name = full_name or search
    if full_name:          params["full_name"] = full_name
    if phone:              params["phone"] = phone
    if is_online is not None: params["is_online"] = is_online
    return _get("/admin/employees", params=params)


def admin_create_employee(full_name: str, phone: str, created_by: int = None, password: str = None,
                          role_id: int = None, account_type: str = "GUARD") -> dict:
    """POST /api/v1/admin/employees"""
    body = {"full_name": full_name, "phone": phone, "created_by": created_by}
    if password: body["password"] = password
    if role_id is not None: body["role_id"] = role_id
    if account_type: body["accout_type"] = account_type
    return _post("/admin/employees", body)


def admin_get_employee(emp_id: int) -> dict:
    """GET /api/v1/admin/employees/{id}"""
    return _get(f"/admin/employees/{emp_id}")


def admin_update_employee(emp_id: int, body: dict) -> dict:
    """PUT /api/v1/admin/employees/{id}"""
    return _put(f"/admin/employees/{emp_id}", body)


def admin_delete_employee(emp_id: int) -> dict:
    """DELETE /api/v1/admin/employees/{id}"""
    return _delete(f"/admin/employees/{emp_id}")


# ════════════════════════════════════════════════════
# PHẦN 2.2: ADMIN — GROUP PROFILES
# ════════════════════════════════════════════════════

def admin_get_group_profiles(search=None, page=0, size=100, profile_code=None, profile_name=None):
    profile_name = profile_name or search
    return _get("/admin/group-profiles", params={
        "page": page, "size": size,
        "profile_code": profile_code, "profile_name": profile_name
    })

def admin_get_roles(page=0, size=1000):
    return _get("/admin/roles", params={"page": page, "size": size})



def admin_create_group_profile(profile_code: str, profile_name: str) -> dict:
    """POST /api/v1/admin/group-profiles"""
    return _post("/admin/group-profiles", {"profile_code": profile_code, "profile_name": profile_name})


def admin_update_group_profile(profile_id: int, profile_name: str, profile_code: str = None) -> dict:
    """PUT /api/v1/admin/group-profiles/{id}"""
    return _put(f"/admin/group-profiles/{profile_id}", {
        "profile_code": profile_code,
        "profile_name": profile_name,
    })


def admin_delete_group_profile(profile_id: int) -> dict:
    """DELETE /api/v1/admin/group-profiles/{id}"""
    return _delete(f"/admin/group-profiles/{profile_id}")


# ════════════════════════════════════════════════════
# PHẦN 2.2: ADMIN — CUSTOMER GROUPS
# ════════════════════════════════════════════════════

def admin_get_customer_groups(page=0, size=20, search=None, group_name=None, group_code=None,
                              group_id=None, is_synchronize=None, profile_id=None,
                              master_account_id=None, master_account_name=None,
                              master_account_phone=None) -> dict:
    """GET /api/v1/admin/customer-groups"""
    params = {"page": page, "size": size}
    group_name = group_name or search
    if group_id not in (None, ""): params["id"] = group_id
    if group_name: params["group_name"] = group_name
    if group_code: params["group_code"] = group_code
    if is_synchronize is not None: params["is_synchronize"] = is_synchronize
    if profile_id not in (None, ""): params["profile_id"] = profile_id
    if master_account_id not in (None, ""): params["master_account_id"] = master_account_id
    if master_account_name: params["master_account_name"] = master_account_name
    if master_account_phone: params["master_account_phone"] = master_account_phone
    return _get("/admin/customer-groups", params=params)


def admin_create_customer_group(profile_id: int, group_code: str, group_name: str,
                                created_by: int, master_account_id: int = None,
                                is_synchronize: bool = None,
                                created_at: str = None) -> dict:
    """POST /api/v1/admin/customer-groups"""
    return _post("/admin/customer-groups", {
        "profile_id": profile_id, "group_code": group_code,
        "group_name": group_name, "created_by": created_by,
        "master_account_id": master_account_id,
        "created_at": created_at,
        "is_synchronize": is_synchronize,
    })


def admin_update_customer_group(group_id: int, body: dict) -> dict:
    """PUT /api/v1/admin/customer-groups/{id}"""
    return _put(f"/admin/customer-groups/{group_id}", body)


def admin_delete_customer_group(group_id: int) -> dict:
    """DELETE /api/v1/admin/customer-groups/{id}"""
    return _delete(f"/admin/customer-groups/{group_id}")


# ════════════════════════════════════════════════════
# PHẦN 2.2: ADMIN — CUSTOMERS
# ════════════════════════════════════════════════════

def admin_get_customers(page=0, size=20, search=None, full_name=None, phone=None,
                        address=None, group_id=None, account_id=None, group_name=None, deleted=None) -> dict:
    """GET /api/v1/admin/customers"""
    params = {"page": page, "size": size}
    full_name = full_name or search
    if full_name: params["full_name"] = full_name
    if phone: params["phone"] = phone
    if address: params["address"] = address
    if group_id not in (None, ""): params["group_id"] = group_id
    if account_id not in (None, ""): params["account_id"] = account_id
    if group_name: params["group_name"] = group_name
    if deleted is not None: params["deleted"] = deleted
    return _get("/admin/customers", params=params)


def admin_get_customer(customer_id: int) -> dict:
    """GET /api/v1/admin/customers/{id}"""
    return _get(f"/admin/customers/{customer_id}")


def admin_create_customer(group_id: int, full_name: str, phone: str, address: str = None, password: str = None,
                          role_id: int = None, created_by: int = None, account_type: str = None) -> dict:
    """POST /api/v1/admin/customers"""
    body = {"group_id": group_id, "full_name": full_name, "phone": phone}
    if address:  body["address"]  = address
    if password: body["password"] = password
    if role_id is not None: body["role_id"] = role_id
    if created_by is not None: body["created_by"] = created_by
    if account_type: body["accout_type"] = account_type
    return _post("/admin/customers", body)


def admin_update_customer(customer_id: int, body: dict) -> dict:
    """PUT /api/v1/admin/customers/{id}"""
    return _put(f"/admin/customers/{customer_id}", body)


def admin_delete_customer(customer_id: int) -> dict:
    """DELETE /api/v1/admin/customers/{id}"""
    return _delete(f"/admin/customers/{customer_id}")


# ════════════════════════════════════════════════════
# PHẦN 2.2: ADMIN — COMPLAINTS
# ════════════════════════════════════════════════════

def admin_get_complaints(page=0, size=10, created_from=None, created_to=None, created_by=None, solved_by=None, is_solved=None) -> dict:
    """GET /api/v1/admin/complaints"""
    return _get("/admin/complaints", params={
        "page": page,
        "size": size,
        "created_from": created_from,
        "created_to": created_to,
        "created_by": created_by,
        "solved_by": solved_by,
        "is_solved": is_solved,
    })


def admin_get_complaint(complaint_id: int) -> dict:
    """GET /api/v1/admin/complaints/{id}"""
    return _get(f"/admin/complaints/{complaint_id}")


def admin_solve_complaint(complaint_id: int) -> dict:
    """PUT /api/v1/admin/complaints/{id}/solve"""
    resp = requests.put(f"{BASE_URL}/admin/complaints/{complaint_id}/solve", headers=_headers(), timeout=10)
    resp.raise_for_status()
    return _decode_response(resp)


def admin_solve_complaint(complaint_id: int) -> dict:
    """PUT /api/v1/admin/complaints/{id}/solve"""
    return _put(f"/admin/complaints/{complaint_id}/solve")


# ════════════════════════════════════════════════════
# PHẦN 2.3: ADMIN — VEHICLE TYPES
# ════════════════════════════════════════════════════

# ── AdminSubscriptionController base: /api/v1/admin/subscription ──

def admin_get_vehicle_types(search=None, page=0, size=10) -> dict:
    """GET /api/v1/admin/subscription/vehicle-types"""
    return _get("/admin/subscription/vehicle-types", params={"keyword": search, "page": page, "size": size}, camelize_params=False)


def admin_get_vehicle_type(vt_id: int) -> dict:
    """GET /api/v1/admin/subscription/vehicle-types/{id}"""
    return _get(f"/admin/subscription/vehicle-types/{vt_id}")


def admin_create_vehicle_type(type_code: str, type_name: str) -> dict:
    """POST /api/v1/admin/subscription/vehicle-types"""
    return _post("/admin/subscription/vehicle-types", {"type_code": type_code, "type_name": type_name})


def admin_update_vehicle_type(vt_id: int, type_name: str, type_code: str = None) -> dict:
    """PUT /api/v1/admin/subscription/vehicle-types/{id}"""
    body = {"type_name": type_name}
    if type_code is not None:
        body["type_code"] = type_code
    return _put(f"/admin/subscription/vehicle-types/{vt_id}", body)


def admin_delete_vehicle_type(vt_id: int) -> dict:
    """DELETE /api/v1/admin/subscription/vehicle-types/{id}"""
    return _delete(f"/admin/subscription/vehicle-types/{vt_id}")


# ════════════════════════════════════════════════════
# PHẦN 2.3: ADMIN — PACKAGES
# ════════════════════════════════════════════════════

def admin_get_packages(search=None, status=None, page=0, size=10, group_profile_id=None) -> dict:
    """GET /api/v1/admin/subscription/packages"""
    params = {"searchName": search, "status": status, "page": page, "size": size}
    if group_profile_id is not None:
        params["groupProfileId"] = group_profile_id
    return _get("/admin/subscription/packages", params=params, camelize_params=False)


def admin_create_package(package_code: str, package_name: str, description: str = None,
                         profile_id: int = None, is_available: bool = True) -> dict:
    """POST /api/v1/admin/subscription/packages"""
    return _post("/admin/subscription/packages", {
        "packageCode": package_code, "packageName": package_name,
        "description": description, "profileId": profile_id,
        "isAvailable": is_available,
    })


def admin_update_package(pkg_id: int, package_name: str, description: str = None,
                         package_code: str = None, profile_id: int = None,
                         is_available: bool = None) -> dict:
    """PUT /api/v1/admin/subscription/packages/{id}"""
    return _put(f"/admin/subscription/packages/{pkg_id}", {
        "packageCode": package_code,
        "packageName": package_name,
        "description": description,
        "profileId": profile_id,
        "isAvailable": is_available,
    })


def admin_delete_package(pkg_id: int) -> dict:
    """DELETE /api/v1/admin/subscription/packages/{id}"""
    return _delete(f"/admin/subscription/packages/{pkg_id}")


def admin_get_package_details(pkg_id: int) -> dict:
    """GET /api/v1/admin/subscription/packages/{id}/details"""
    return _get(f"/admin/subscription/packages/{pkg_id}/details")


# ════════════════════════════════════════════════════
# PHẦN 2.3: ADMIN — PACKAGE VEHICLE TYPES (QUOTA)
# ════════════════════════════════════════════════════

def admin_get_package_vehicle_types(pvt_id: int) -> dict:
    """GET /api/v1/admin/subscription/package-vehicle-types/{id}"""
    return _get(f"/admin/subscription/package-vehicle-types/{pvt_id}")


def admin_create_package_vehicle_type(profile_id: int, package_id: int, vehicle_type_id: int, max_quantity: int) -> dict:
    """POST /api/v1/admin/subscription/package-vehicle-types"""
    return _post("/admin/subscription/package-vehicle-types", {
        "profileId": profile_id, "packageId": package_id,
        "vehicleTypeId": vehicle_type_id, "maxQuantity": max_quantity
    })


def admin_update_package_vehicle_type(pvt_id: int, max_quantity: int,
                                      package_id: int = None, vehicle_type_id: int = None) -> dict:
    """PUT /api/v1/admin/subscription/package-vehicle-types/{id}"""
    return _put(f"/admin/subscription/package-vehicle-types/{pvt_id}", {
        "packageId": package_id,
        "vehicleTypeId": vehicle_type_id,
        "maxQuantity": max_quantity,
    })


def admin_delete_package_vehicle_type(pvt_id: int) -> dict:
    """DELETE /api/v1/admin/subscription/package-vehicle-types/{id}"""
    return _delete(f"/admin/subscription/package-vehicle-types/{pvt_id}")


# ════════════════════════════════════════════════════
# PHẦN 2.3: ADMIN — PACKAGE PRICES
# ════════════════════════════════════════════════════

def admin_get_package_price(price_id: int) -> dict:
    """GET /api/v1/admin/subscription/prices/{id}"""
    return _get(f"/admin/subscription/prices/{price_id}")


def admin_get_package_prices(pvt_id: int) -> dict:
    """GET /api/v1/admin/subscription/prices/{id}
    Backward-compatible alias; Java treats {id} as the package price id.
    """
    return admin_get_package_price(pvt_id)


def admin_create_package_price(pkg_veh_type_id: int, package_price_name: str, duration_months: int, price: float, is_active: bool = True) -> dict:
    """POST /api/v1/admin/subscription/prices"""
    return _post("/admin/subscription/prices", {
        "pkgVehTypeId": pkg_veh_type_id, "packagePriceName": package_price_name,
        "durationMonths": duration_months, "price": price, "isActive": is_active
    })


def admin_update_package_price(price_id: int, price: float, is_active: bool,
                               pkg_veh_type_id: int = None, package_price_name: str = None,
                               duration_months: int = None) -> dict:
    """PUT /api/v1/admin/subscription/prices/{id}"""
    return _put(f"/admin/subscription/prices/{price_id}", {
        "pkgVehTypeId": pkg_veh_type_id,
        "packagePriceName": package_price_name,
        "durationMonths": duration_months,
        "price": price,
        "isActive": is_active,
    })


def admin_delete_package_price(price_id: int) -> dict:
    """DELETE /api/v1/admin/subscription/prices/{id}"""
    return _delete(f"/admin/subscription/prices/{price_id}")


# ════════════════════════════════════════════════════
# PHẦN 2.3: ADMIN — TARIFF RULES
# ════════════════════════════════════════════════════

def admin_get_tariff_rules(vehicle_type_id: int = None, day_type: str = None,
                           is_active: bool = None, page: int = 0, size: int = 100,
                           sort: str = "id,asc") -> dict:
    """POST /api/v1/admin/tariff-rules/filter"""
    body = {}
    if vehicle_type_id not in (None, ""): body["vehicleTypeId"] = vehicle_type_id
    if day_type is not None:        body["dayType"]       = day_type
    if is_active is not None:       body["isActive"]      = is_active
    return _post(f"/admin/tariff-rules/filter?page={page}&size={size}&sort={sort}", body)


def admin_get_tariff_rule(rule_id: int) -> dict:
    """GET /api/v1/admin/tariff-rules/{id}"""
    return _get(f"/admin/tariff-rules/{rule_id}")


def admin_create_tariff_rule(vehicle_type_id: int, day_type: str, start_time: str, end_time: str, base_price: float, is_active: bool = True) -> dict:
    """POST /api/v1/admin/tariff-rules"""
    return _post("/admin/tariff-rules", {
        "vehicleTypeId": vehicle_type_id, "dayType": day_type,
        "startTime": start_time, "endTime": end_time,
        "basePrice": base_price, "isActive": is_active
    })


def admin_update_tariff_rule(rule_id: int, body: dict) -> dict:
    """PUT /api/v1/admin/tariff-rules/{id}"""
    return _put(f"/admin/tariff-rules/{rule_id}", body)


def admin_disable_tariff_rule(rule_id: int) -> dict:
    """PATCH /api/v1/admin/tariff-rules/{id}/disable — Java has no DELETE, only disable"""
    return _patch(f"/admin/tariff-rules/{rule_id}/disable")


# ════════════════════════════════════════════════════
# PHẦN 2.4: ADMIN — ZONES & DEVICES
# ════════════════════════════════════════════════════

def admin_get_zones(parent_zone_id=None, zone_type=None) -> dict:
    """GET /api/v1/admin/zones"""
    params = {}
    if parent_zone_id: params["parent_zone_id"] = parent_zone_id
    if zone_type:      params["zone_type"]       = zone_type
    return _wrap_data(_get("/admin/zones", params=params or None))


def admin_get_zone(zone_id: int) -> dict:
    """GET /api/v1/admin/zones/{id}"""
    return _get(f"/admin/zones/{zone_id}")


def admin_create_zone(parent_zone_id, zone_name: str, zone_type: str, capacity: int, current_occupancy: int = 0) -> dict:
    """POST /api/v1/admin/zones"""
    return _post("/admin/zones", {
        "parent_zone_id": parent_zone_id, "zone_name": zone_name,
        "zone_type": zone_type, "capacity": capacity, "current_occupancy": current_occupancy
    })


def admin_update_zone(zone_id: int, body: dict) -> dict:
    """PUT /api/v1/admin/zones/{id}"""
    return _put(f"/admin/zones/{zone_id}", body)


def admin_delete_zone(zone_id: int) -> dict:
    """DELETE /api/v1/admin/zones/{id}"""
    return _delete(f"/admin/zones/{zone_id}")


def admin_get_devices(status: str = None) -> dict:
    """GET /api/v1/admin/devices
    Java AdminDeviceController.listDevices() takes NO query params.
    Returns List<IoTDevice> directly (not ApiResponse wrapped)."""
    resp = requests.get(f"{BASE_URL}/admin/devices", headers=_headers(), timeout=10)
    resp.raise_for_status()
    # Returns raw list, wrap to match caller expectations
    data = _decode_response(resp)
    if status and isinstance(data, list):
        data = [d for d in data if str(d.get("status", "")).upper() == status.upper()]
    if isinstance(data, list):
        return {"data": data}
    return data


def admin_get_device(device_id: int) -> dict:
    """GET /api/v1/admin/devices/{id}"""
    return _wrap_data(_get(f"/admin/devices/{device_id}"))


def admin_get_devices_by_zone(zone_id: int) -> dict:
    """GET /api/v1/admin/devices/zones/{id}"""
    resp = requests.get(f"{BASE_URL}/admin/devices/zones/{zone_id}", headers=_headers(), timeout=10)
    resp.raise_for_status()
    data = _decode_response(resp)
    return {"data": data} if isinstance(data, list) else data


def admin_create_device(body: dict) -> dict:
    """POST /api/v1/admin/devices"""
    return _post("/admin/devices", body)


def admin_update_device(device_id: int, body: dict) -> dict:
    """PUT /api/v1/admin/devices/{id}"""
    return _put(f"/admin/devices/{device_id}", body)


def admin_delete_device(device_id: int) -> dict:
    """DELETE /api/v1/admin/devices/{id}"""
    return _delete(f"/admin/devices/{device_id}")


def admin_barrier_control(device_id: int, command: str) -> dict:
    """POST /api/v1/admin/devices/{id}/barrier-control"""
    return _wrap_data(_post(f"/admin/devices/{device_id}/barrier-control", {"command": command}))


# ════════════════════════════════════════════════════
# PHẦN 2.5: ADMIN — BOOKINGS & BOOKING DETAILS
# ════════════════════════════════════════════════════

# ── EmployeeBookingController: /api/v1/employee/bookings ──
# NOTE: Java has NO /admin/bookings — controller is EmployeeBookingController

def admin_get_bookings(page=0, size=20, group_id=None, package_id=None,
                       search=None, sort: str = None) -> dict:
    """GET /api/v1/employee/bookings"""
    params = {"page": page, "size": size}
    if sort: params["sort"] = sort
    if group_id not in (None, ""): params["group_id"] = group_id
    if package_id not in (None, ""): params["package_id"] = package_id
    if search:     params["search"]     = search
    try:
        return _get("/employee/bookings", params=params)
    except requests.HTTPError as ex:
        if ex.response is None or ex.response.status_code < 500:
            raise
        return _bookings_from_details_fallback(page, size, group_id, package_id, search)


def _bookings_from_details_fallback(page=0, size=20, group_id=None, package_id=None, search=None) -> dict:
    """Fallback for /employee/bookings 500.

    Java currently has a fragile DTO query on the booking list endpoint. The
    booking-detail endpoint returns a stable DTO, so group rows by booking_id
    to keep the Hợp Đồng page usable without changing Java.
    """
    detail_resp = admin_get_booking_details(
        page=0,
        size=max(100, (page + 1) * size * 3),
        group_id=group_id,
        package_id=package_id,
        sort=None,
    )
    data = detail_resp.get("data", {})
    details = data.get("content", []) if isinstance(data, dict) else []
    search_text = (search or "").strip().lower()
    grouped: dict[int, dict] = {}
    for detail in details:
        booking_id = detail.get("booking_id") or detail.get("bookingId")
        if booking_id in (None, ""):
            continue
        label = (
            detail.get("customer_name")
            or detail.get("customerName")
            or detail.get("customer_phone")
            or detail.get("customerPhone")
            or f"Hợp Đồng #{booking_id}"
        )
        package_name = detail.get("package_price_name") or detail.get("packagePriceName") or "-"
        vehicle_no = detail.get("vehicle_no") or detail.get("vehicleNo") or ""
        haystack = f"{label} {package_name} {vehicle_no} {booking_id}".lower()
        if search_text and search_text not in haystack:
            continue
        grouped.setdefault(int(booking_id), {
            "id": int(booking_id),
            "created_at": detail.get("created_at") or detail.get("createdAt"),
            "group_name": label,
            "group_code": vehicle_no,
            "package_id": detail.get("package_price_id") or detail.get("packagePriceId"),
            "package_name": package_name,
            "created_by": None,
            "creator_name": "-",
        })

    rows = list(grouped.values())
    total = len(rows)
    start = page * size
    end = start + size
    return {
        "data": {
            "content": rows[start:end],
            "totalElements": total,
            "totalPages": max(1, (total + size - 1) // size),
        }
    }


def admin_get_booking(booking_id: int) -> dict:
    """GET /api/v1/employee/bookings/{id}"""
    return _get(f"/employee/bookings/{booking_id}")


def admin_get_booking_and_details(booking_id: int) -> dict:
    """GET /api/v1/employee/bookings/{id}/details"""
    try:
        return _get(f"/employee/bookings/{booking_id}/details")
    except requests.HTTPError as ex:
        if ex.response is None or ex.response.status_code < 500:
            raise
        detail_resp = admin_get_booking_details(page=0, size=500, sort=None)
        data = detail_resp.get("data", {})
        details = data.get("content", []) if isinstance(data, dict) else []
        details = [
            detail for detail in details
            if int(detail.get("booking_id") or detail.get("bookingId") or -1) == int(booking_id)
        ]
        return {
            "data": {
                "booking_info": {"id": booking_id},
                "details": details,
            }
        }


def admin_create_booking(group_id: int, package_id: int, created_by: int) -> dict:
    """POST /api/v1/employee/bookings"""
    return _post("/employee/bookings", {"group_id": group_id, "package_id": package_id})


def admin_update_booking(booking_id: int, body: dict) -> dict:
    """PUT /api/v1/employee/bookings/{id}"""
    return _put(f"/employee/bookings/{booking_id}", body)


def admin_delete_booking(booking_id: int) -> dict:
    """DELETE /api/v1/employee/bookings/{id}"""
    return _delete(f"/employee/bookings/{booking_id}")


def admin_get_booking_details(page=0, size=20, group_id=None, package_id=None,
                              booking_id=None, customer_id=None, status=None,
                              sort: str = "id,desc") -> dict:
    """GET /api/v1/admin/booking-details"""
    params = {"page": page, "size": size}
    if sort: params["sort"] = sort
    if group_id not in (None, ""): params["group_id"] = group_id
    if package_id not in (None, ""): params["package_id"] = package_id
    # These are retained for caller compatibility; current Java ignores them.
    if booking_id:  params["booking_id"]  = booking_id
    if customer_id: params["customer_id"] = customer_id
    if status:      params["status"]      = status
    return _get("/admin/booking-details", params=params)


def admin_get_booking_detail(detail_id: int) -> dict:
    """GET /api/v1/admin/booking-details/{id}"""
    return _get(f"/admin/booking-details/{detail_id}")


def admin_create_booking_detail(body: dict) -> dict:
    """POST /api/v1/admin/booking-details"""
    return _post("/admin/booking-details", body)


def admin_update_booking_detail(detail_id: int, body: dict) -> dict:
    """PUT /api/v1/admin/booking-details/{id}"""
    return _put(f"/admin/booking-details/{detail_id}", body)


def admin_delete_booking_detail(detail_id: int) -> dict:
    """DELETE /api/v1/admin/booking-details/{id}"""
    return _delete(f"/admin/booking-details/{detail_id}")


# ════════════════════════════════════════════════════
# PHẦN 2.6: ADMIN — REPORTS & PARKING SESSIONS
# ════════════════════════════════════════════════════

def admin_get_parking_sessions(page=0, size=20, vehicle_no=None, customer_id=None,
                                from_time=None, to_time=None, flag_manual=None,
                                entry_time_from=None, entry_time_to=None,
                                exit_time_from=None, exit_time_to=None,
                                booking_detail_id=None, vehicle_type_id=None,
                                zone_in_id=None, zone_out_id=None,
                                paid_greater_than=None, paid_less_than=None,
                                is_currently_parked=None,
                                sort: str = "entryTime,desc") -> dict:
    """GET /api/v1/admin/parking-sessions"""
    params = {"page": page, "size": size}
    if sort: params["sort"] = sort
    if vehicle_no:  params["vehicle_no"]  = vehicle_no
    if customer_id not in (None, ""): params["customer_id"] = customer_id
    if from_time:   params["entry_time_from"] = from_time
    if to_time:     params["entry_time_to"]   = to_time
    if entry_time_from: params["entry_time_from"] = entry_time_from
    if entry_time_to:   params["entry_time_to"]   = entry_time_to
    if exit_time_from:  params["exit_time_from"]  = exit_time_from
    if exit_time_to:    params["exit_time_to"]    = exit_time_to
    if flag_manual is not None: params["flag_manual"] = flag_manual
    if booking_detail_id not in (None, ""): params["booking_detail_id"] = booking_detail_id
    if vehicle_type_id not in (None, ""): params["vehicle_type_id"] = vehicle_type_id
    if zone_in_id not in (None, ""): params["zone_in_id"] = zone_in_id
    if zone_out_id not in (None, ""): params["zone_out_id"] = zone_out_id
    if paid_greater_than not in (None, ""): params["paid_greater_than"] = paid_greater_than
    if paid_less_than not in (None, ""): params["paid_less_than"] = paid_less_than
    if is_currently_parked is not None: params["is_currently_parked"] = is_currently_parked
    return _get("/admin/parking-sessions", params=params)


def admin_get_parking_session(session_id: int) -> dict:
    """GET /api/v1/admin/parking-sessions/{id}"""
    return _get(f"/admin/parking-sessions/{session_id}")


def admin_update_parking_session(session_id: int, correct_vehicle_no=None, update_amount_paid=None, update_vehicle_type_id=None) -> dict:
    """PUT /api/v1/admin/parking-sessions/{id}/manual-update"""
    return _put(f"/admin/parking-sessions/{session_id}/manual-update", {
        "correct_vehicle_no": correct_vehicle_no,
        "update_amount_paid": update_amount_paid,
        "update_vehicle_type_id": update_vehicle_type_id,
    })


def admin_get_payments(page=0, size=20, pay_code=None, payer_id=None, method=None, status=None,
                       customer_id=None, customer_phone=None, gateway=None,
                       parking_session_id=None, min_amount=None, max_amount=None,
                       created_at_from=None, created_at_to=None,
                       updated_at_from=None, updated_at_to=None,
                       sort: str = None) -> dict:
    """POST /api/v1/admin/payments — Java uses @PostMapping + @RequestBody PaymentFilterRequest.
    This is a search endpoint disguised as a list, sends filter as JSON body."""
    body = {}
    if pay_code:  body["pay_code"]  = pay_code
    if payer_id not in (None, ""): body["customer_id"] = payer_id
    if customer_id not in (None, ""): body["customer_id"] = customer_id
    if customer_phone: body["customer_phone"] = customer_phone
    if method:    body["method"]   = method
    if status:    body["pay_status"] = status
    if gateway:   body["gateway"] = gateway
    if parking_session_id not in (None, ""): body["parking_session_id"] = parking_session_id
    if min_amount not in (None, ""): body["min_amount"] = min_amount
    if max_amount not in (None, ""): body["max_amount"] = max_amount
    if created_at_from: body["created_at_from"] = created_at_from
    if created_at_to:   body["created_at_to"]   = created_at_to
    if updated_at_from: body["updated_at_from"] = updated_at_from
    if updated_at_to:   body["updated_at_to"]   = updated_at_to
    params = {"page": page, "size": size}
    if sort: params["sort"] = sort
    try:
        return _post("/admin/payments", body, params=params)
    except requests.HTTPError as ex:
        if ex.response is None or ex.response.status_code < 500 or body:
            raise
        try:
            return _post("/admin/payments", {}, params=None)
        except requests.HTTPError as retry_ex:
            if retry_ex.response is None or retry_ex.response.status_code < 500:
                raise
            return _payments_from_details_fallback(page, size)


def _payments_from_details_fallback(page=0, size=20) -> dict:
    """Fallback for /admin/payments 500 using payment-details."""
    detail_resp = admin_get_payment_details(page=page, size=size, sort=None)
    data = detail_resp.get("data", {})
    details = data.get("content", []) if isinstance(data, dict) else []
    rows = []
    for detail in details:
        payment_id = detail.get("payment_id") or detail.get("paymentId")
        rows.append({
            "id": payment_id or detail.get("id"),
            "payer_id": None,
            "customer_full_name": "-",
            "customer_phone": "-",
            "parking_session_id": None,
            "transaction_id": "-",
            "pay_code": f"Payment #{payment_id}" if payment_id else f"Detail #{detail.get('id', '-')}",
            "amount": detail.get("item_amount") or detail.get("itemAmount") or 0,
            "method": "-",
            "gateway": "-",
            "status": "-",
            "created_at": detail.get("applied_start_date") or detail.get("appliedStartDate"),
            "updated_at": detail.get("applied_end_date") or detail.get("appliedEndDate"),
        })
    return {
        "data": {
            "content": rows,
            "totalElements": data.get("totalElements", len(rows)) if isinstance(data, dict) else len(rows),
            "totalPages": data.get("totalPages", 1) if isinstance(data, dict) else 1,
        }
    }


def admin_get_reconciliation_exceptions(page=0, size=20) -> dict:
    """GET /api/v1/admin/payments/reconciliation-exceptions"""
    return _get("/admin/payments/reconciliation-exceptions", params={"page": page, "size": size})


def admin_get_payment(payment_id: int) -> dict:
    """GET /api/v1/admin/payments/{id}"""
    return _get(f"/admin/payments/{payment_id}")


def admin_get_payment_details_tree(payment_id: int) -> dict:
    """GET /api/v1/admin/payments/{id}/details"""
    return _get(f"/admin/payments/{payment_id}/details")


def admin_resolve_payment(payment_id: int) -> dict:
    """POST /api/v1/admin/payments/{id}/resolve"""
    return _post(f"/admin/payments/{payment_id}/resolve")

def admin_cancel_payment(payment_id: int) -> dict:
    """POST /api/v1/admin/payments/{id}/cancel"""
    return _post(f"/admin/payments/{payment_id}/cancel")


def admin_get_payment_details(page=0, size=20, payment_id=None, booking_detail_id=None,
                              min_item_amount=None, max_item_amount=None,
                              applied_start_date_from=None, applied_start_date_to=None,
                              applied_end_date_from=None, applied_end_date_to=None,
                              sort: str = "id,desc") -> dict:
    """GET /api/v1/admin/payment-details"""
    body = {
        "payment_id": payment_id,
        "booking_detail_id": booking_detail_id,
        "min_item_amount": min_item_amount,
        "max_item_amount": max_item_amount,
        "applied_start_date_from": applied_start_date_from,
        "applied_start_date_to": applied_start_date_to,
        "applied_end_date_from": applied_end_date_from,
        "applied_end_date_to": applied_end_date_to,
    }
    resp = requests.get(
        f"{BASE_URL}/admin/payment-details",
        headers=_headers(),
        json=_clean(body),
        params=_clean({"page": page, "size": size, "sort": sort}, camelize=False),
        timeout=10,
    )
    resp.raise_for_status()
    return _decode_response(resp)


def admin_get_payment_detail(detail_id: int) -> dict:
    """GET /api/v1/admin/payment-details/{id}"""
    return _get(f"/admin/payment-details/{detail_id}")


def admin_report_revenue(from_date: str, to_date: str, group_by: str = "DAY") -> dict:
    """GET /api/v1/admin/reports/revenue (Redirected to POST /admin/revenues/revenue-time-series)"""
    body = {
        "created_at_from": f"{from_date}T00:00:00",
        "created_at_to": f"{to_date}T23:59:59"
    }
    resp = _post("/admin/revenues/revenue-time-series", body)
    
    # Map 'revenue' to 'total_amount' to remain fully compatible with the UI
    if isinstance(resp, dict) and isinstance(resp.get("data"), list):
        for item in resp["data"]:
            if "revenue" in item:
                item["total_amount"] = item["revenue"]
    return resp


def admin_report_kpi(from_date: str, to_date: str) -> dict:
    """POST /api/v1/admin/revenues/kpis"""
    body = {
        "created_at_from": f"{from_date}T00:00:00",
        "created_at_to": f"{to_date}T23:59:59"
    }
    return _post("/admin/revenues/kpis", body)


def admin_report_occupancy(parent_zone_id: int = None) -> dict:
    """GET /api/v1/admin/reports/occupancy (Redirected to active GET /admin/zones)"""
    return _get("/admin/zones")


# ════════════════════════════════════════════════════
# PHẦN 3.1: IOT — Camera/Edge APIs (called by this desktop)
# ════════════════════════════════════════════════════

def upload_image(image_bytes: bytes, filename: str = "capture.jpg") -> dict:
    """POST /api/v1/system/upload-image — multipart/form-data"""
    return _post("/system/upload-image", files={"file": (filename, image_bytes, "image/jpeg")})


def iot_parking_entry(device_code: str = None, vehicle_no: str = None, image_in_url: str = None,
                      vehicle_type_id: int = None, zone_id: int = None, device_id: int = None,
                      image_url: str = None) -> dict:
    """POST /api/v1/iot/parking/entry"""
    body = {
        "vehicle_no": vehicle_no,
        "vehicle_type_id": vehicle_type_id,
        "zone_id": zone_id,
        "device_id": device_id,
        "image_url": image_url or image_in_url,
    }
    return _wrap_data(_post("/iot/parking/entry", body))


def iot_parking_exit(device_code: str = None, vehicle_no: str = None, image_out_url: str = None,
                     zone_id: int = None, image_url: str = None, vehicle_type_id: int = None,
                     device_id: int = None) -> dict:
    """PUT /api/v1/iot/parking/exit — Java uses PUT not POST"""
    body = {
        "vehicle_no": vehicle_no,
        "zone_id": zone_id,
        "device_id": device_id,
        "image_url": image_url or image_out_url,
        "vehicle_type_id": vehicle_type_id,
    }
    return _wrap_data(_put("/iot/parking/exit", body))


def iot_zone_transition(device_id: int) -> dict:
    """PUT /api/v1/iot/zone-transition/{deviceId} — different path, PUT method, deviceId is path var"""
    return _put(f"/iot/zone-transition/{device_id}")


def iot_device_ping(device_code: str, ip_address: str = None) -> dict:
    """POST /api/v1/iot/devices/{deviceCode}/ping"""
    body = {}
    if ip_address: body["ip_address"] = ip_address
    return _post(f"/iot/devices/{device_code}/ping", body)


# ════════════════════════════════════════════════════
# PHẦN 3.2: CUSTOMER / EMPLOYEE / NOTIFICATIONS
# ════════════════════════════════════════════════════

def get_notifications() -> dict:
    """GET /api/v1/notifications"""
    return _wrap_data(_get("/notifications"))


def mark_notification_read(notification_id: int) -> dict:
    """PUT /api/v1/notifications/{id}/read"""
    return _put(f"/notifications/{notification_id}/read")


def mark_all_notifications_read() -> dict:
    """PUT /api/v1/notifications/read-all"""
    return _put("/notifications/read-all")


def customer_get_me() -> dict:
    """GET /api/v1/customer/me"""
    return _get("/customer/me")


def customer_update_me(body: dict) -> dict:
    """PUT /api/v1/customer/me"""
    return _put("/customer/me", body)


def employee_get_me() -> dict:
    """GET /api/v1/employee/me"""
    return _get("/employee/me")


def employee_update_me(body: dict) -> dict:
    """PUT /api/v1/employee/me"""
    return _put("/employee/me", body)


def master_get_customers(page=0, size=10, group_name=None, **filters) -> dict:
    """GET /api/v1/master/customer"""
    params = {"page": page, "size": size, "group_name": group_name}
    params.update(filters)
    return _get("/master/customer", params=params)


def master_create_customer(body: dict) -> dict:
    """POST /api/v1/master/customer"""
    return _post("/master/customer", body)


def master_update_customer(customer_id: int, body: dict) -> dict:
    """PUT /api/v1/master/customer/{id}"""
    return _put(f"/master/customer/{customer_id}", body)


def master_delete_customer(customer_id: int) -> dict:
    """DELETE /api/v1/master/customer/{id}"""
    return _delete(f"/master/customer/{customer_id}")


def customer_create_complaint(content: str, img_url: str = None) -> dict:
    """POST /api/v1/customer/complaints"""
    return _post("/customer/complaints", {"content": content, "img_url": img_url})


def customer_get_home() -> dict:
    """GET /api/v1/customer/home"""
    return _get("/customer/home")


def customer_get_booking() -> dict:
    """GET /api/v1/customer/operation/booking"""
    return _get("/customer/operation/booking")


def customer_create_booking_detail(body: dict) -> dict:
    """POST /api/v1/customer/operation"""
    return _post("/customer/operation", body)


def customer_get_drafts() -> dict:
    """GET /api/v1/customer/operation/drafts"""
    return _get("/customer/operation/drafts")


def customer_clear_drafts() -> dict:
    """DELETE /api/v1/customer/operation/drafts"""
    return _delete("/customer/operation/drafts")


def customer_get_parking_sessions(page=0, size=20, **filters) -> dict:
    """GET /api/v1/customer/parking-sessions"""
    params = {"page": page, "size": size}
    params.update(filters)
    return _get("/customer/parking-sessions", params=params)


def customer_get_parking_session(session_id: int) -> dict:
    """GET /api/v1/customer/parking-sessions/{id}"""
    return _get(f"/customer/parking-sessions/{session_id}")


def customer_get_allowed_vehicle_types() -> dict:
    """GET /api/v1/customer/subscription/metadata/allowed-vehicle-types"""
    return _get("/customer/subscription/metadata/allowed-vehicle-types")


def customer_get_available_packages(vehicle_type_id: int) -> dict:
    """GET /api/v1/customer/subscription/metadata/available-packages"""
    return _get("/customer/subscription/metadata/available-packages", params={"vehicle_type_id": vehicle_type_id})


def customer_get_payments(page=0, size=20, **filters) -> dict:
    """POST /api/v1/customer/payments"""
    return _post("/customer/payments", filters, params={"page": page, "size": size})


def customer_get_payment_details(payment_id: int) -> dict:
    """POST /api/v1/customer/payments/{id}/details"""
    return _post(f"/customer/payments/{payment_id}/details")


def customer_cancel_payment(payment_id: int) -> dict:
    """POST /api/v1/customer/payments/{id}/cancel"""
    return _post(f"/customer/payments/{payment_id}/cancel")


def customer_initiate_booking_payment(items: list[dict], gateway: str = None, return_url: str = None) -> dict:
    """POST /api/v1/customer/payments/booking"""
    return _post("/customer/payments/booking", {"items": items, "gateway": gateway, "return_url": return_url})


def customer_checkout(booking_detail_ids: list[int]) -> dict:
    """POST /api/v1/customer/payments/checkout"""
    return _post("/customer/payments/checkout", {"booking_detail_ids": booking_detail_ids})


def system_initiate_session_payment(parking_session_id: int, gateway: str = None, return_url: str = None, method: str = None) -> dict:
    """POST /api/v1/system/payments/session"""
    return _post("/system/payments/session", {
        "parking_session_id": parking_session_id,
        "gateway": gateway,
        "return_url": return_url,
        "method": method,
    })


def system_sepay_webhook(body: dict, authorization: str) -> dict:
    """POST /api/v1/system/payments/webhook/sepay"""
    headers = {"Authorization": authorization, "Content-Type": "application/json"}
    resp = requests.post(f"{BASE_URL}/system/payments/webhook/sepay", headers=headers, json=_clean(body), timeout=10)
    resp.raise_for_status()
    return _decode_response(resp)


def system_check_payment_status(pay_code: str) -> dict:
    """GET /api/v1/system/payments/{payCode}/status"""
    return _get(f"/system/payments/{pay_code}/status")


# ════════════════════════════════════════════════════
# PHẦN 3.2: GUARD — Guard Kiosk APIs
# ════════════════════════════════════════════════════

def guard_confirm_cash(vehicle_no: str, amount: float) -> dict:
    """POST /api/v1/guard/payments/cash"""
    resp = _post("/guard/payments/cash", {"vehicleNo": vehicle_no, "amount": amount}, camelize_body=False)
    if isinstance(resp, dict) and "data" not in resp:
        out = dict(resp)
        out["data"] = resp
        return out
    return _wrap_data(resp)


def guard_confirm_bank_transfer(vehicle_no: str, amount: float, note: str = None) -> dict:
    """POST /api/v1/guard/payments/bank-transfer"""
    body = {"vehicleNo": vehicle_no, "amount": amount}
    if note:
        body["note"] = note
    resp = _post("/guard/payments/bank-transfer", body, camelize_body=False)
    if isinstance(resp, dict) and "data" not in resp:
        out = dict(resp)
        out["data"] = resp
        return out
    return _wrap_data(resp)


def guard_calculate_fee(parking_session_id: int) -> dict:
    """GET /api/v1/guard/parking/calculate-fee"""
    return _get("/guard/parking/calculate-fee", params={"parking_session_id": parking_session_id}, camelize_params=False)


def guard_manual_open(device_code: str, parking_session_id: int = None, reason: str = None) -> dict:
    """POST /api/v1/guard/parking/manual-open"""
    body = {"device_code": device_code}
    if parking_session_id: body["parking_session_id"] = parking_session_id
    if reason:             body["reason"]             = reason
    return _post("/guard/parking/manual-open", body)


def guard_fix_vehicle_no(session_id: int, vehicle_no: str) -> dict:
    """PUT /api/v1/guard/parking-sessions/{id}/vehicle-no"""
    return _put(f"/guard/parking-sessions/{session_id}/vehicle-no", {"vehicle_no": vehicle_no})

