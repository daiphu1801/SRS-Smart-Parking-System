FROM ubuntu:20.04 AS build-env

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    apt-get install -y curl git unzip xz-utils zip libglu1-mesa && \
    rm -rf /var/lib/apt/lists/*

RUN git clone https://github.com/flutter/flutter.git /usr/local/flutter
ENV PATH="/usr/local/flutter/bin:/usr/local/flutter/bin/cache/dart-sdk/bin:${PATH}"
RUN flutter channel stable && flutter upgrade

WORKDIR /app

COPY . .

RUN flutter pub get
RUN touch .env
RUN flutter build web --release --dart-define=API_BASE_URL=__API_BASE_URL_PLACEHOLDER__

FROM nginx:alpine

RUN rm -rf /usr/share/nginx/html/*

COPY --from=build-env /app/build/web /usr/share/nginx/html

EXPOSE 80

COPY entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/bin/sh", "/entrypoint.sh"]
