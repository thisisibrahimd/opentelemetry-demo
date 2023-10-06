FROM nginx:1.24.0
RUN set -x \
    && apt-get update && apt-get install -y \
        wget \
    && rm -rf /var/lib/apt/lists/*

# Use confd to render our config templates since Prometheus doesn't support env vars in their yaml
RUN mkdir -p /usr/local/bin && \
    wget -O /usr/local/bin/confd https://github.com/kelseyhightower/confd/releases/download/v0.16.0/confd-0.16.0-linux-amd64 && \
    /bin/chmod +x /usr/local/bin/confd

COPY --chmod=775 src/frontendproxy-nginx/40-confd.sh /docker-entrypoint.d/40-confd.sh
COPY src/frontendproxy-nginx/nginx.toml /etc/confd/conf.d/nginx.toml
COPY src/frontendproxy-nginx/nginx.conf.tmpl /etc/confd/templates/nginx.conf.tmpl
