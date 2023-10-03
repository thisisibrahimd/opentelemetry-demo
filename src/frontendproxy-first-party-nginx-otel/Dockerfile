FROM nginx:1.24.0 as builder

ENV NGINX_VERSION=1.24.0
ENV GRPC_CPP_SHA=a3f10052090539cd3e19aa8e04f3bf8eceae2964
ENV GRPC_CPP_VERSION=v1.52.0
ENV OTEL_CPP_SHA=d56a5c702fc2154ef82400df4801cd511ffb63ea
ENV OTEL_CPP_VERSION=v1.8.2

RUN set -x \
    && apt-get update && apt-get install -y \
        build-essential \
        ca-certificates \
        cmake \
        curl \
        git \
        gnupg1 \
        libc-ares-dev \
        libre2-dev \
        libpcre3-dev \
        libcurl4-openssl-dev \
        libssl-dev \
        libsystemd-dev \
        pkg-config \
        zlib1g-dev

WORKDIR /opt
RUN set -x \
    && curl -s http://nginx.org/download/nginx-${NGINX_VERSION}.tar.gz -o nginx-${NGINX_VERSION}.tar.gz \
    && tar -xvf nginx-${NGINX_VERSION}.tar.gz \
    && cd nginx-${NGINX_VERSION} \
    && ./configure --with-compat \
    && cd .. \
    && git clone --shallow-submodules --depth 1 --recurse-submodules https://github.com/nginxinc/nginx-otel.git \
    && cd nginx-otel \
    && sed -i 's/18dda3c586b2607d8daead6b97922e59d867bb7d # v1.46.6/${GRPC_CPP_SHA} # ${GRPC_CPP_VERSION}/g' CMakeLists.txt \
    && sed -i 's/57bf8c2b0e85215a61602f559522d08caa4d2fb8 # v1.8.1/${OTEL_CPP_SHA} # ${OTEL_CPP_VERSION}/g' CMakeLists.txt \
    && mkdir build \
    && cd build \
    && cmake \
        -DNGX_OTEL_NGINX_BUILD_DIR=/opt/nginx-${NGINX_VERSION}/objs \
        .. \
    && make -j4

FROM nginx:1.24.0
COPY --from=builder /opt/nginx-otel/build/ngx_otel_module.so /usr/lib/nginx/modules/ngx_otel_module.so

RUN set -x \
    && apt-get update && apt-get install -y \
        wget \
        libc-ares-dev \
        libre2-dev \
    && rm -rf /var/lib/apt/lists/*

# Use confd to render our config templates since Prometheus doesn't support env vars in their yaml
RUN mkdir -p /usr/local/bin && \
    wget -O /usr/local/bin/confd https://github.com/kelseyhightower/confd/releases/download/v0.16.0/confd-0.16.0-linux-amd64 && \
    /bin/chmod +x /usr/local/bin/confd

COPY --chmod=775 src/frontendproxy-first-party-nginx-otel/40-confd.sh /docker-entrypoint.d/40-confd.sh
COPY src/frontendproxy-first-party-nginx-otel/nginx.toml /etc/confd/conf.d/nginx.toml
COPY src/frontendproxy-first-party-nginx-otel/nginx.conf.tmpl /etc/confd/templates/nginx.conf.tmpl
