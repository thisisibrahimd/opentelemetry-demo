FROM nginx:1.24.0 as builder

ENV NGINX_VERSION=1.24.0

RUN set -x \
    && apt-get update && apt-get install -y \
        build-essential \
        ca-certificates \
        cmake \
        curl \
        git \
        gnupg1 \
        libc-ares-dev \
        libcurl4-openssl-dev \
        libpcre3-dev \
        libre2-dev \
        libssl-dev \
        libsystemd-dev \
        pkg-config \
        zlib1g-dev \
        autoconf \
        libtool \
        pkg-config \
        gcc \
        g++ \
        gnupg2 \
        lsb-release \
        apt-transport-https \
        software-properties-common

WORKDIR /opt

RUN git clone --shallow-submodules --depth 1 --recurse-submodules -b v1.49.2 \
  https://github.com/grpc/grpc \
  && cd grpc \
  && mkdir -p cmake/build \
  && cd cmake/build \
  && cmake \
    -DgRPC_INSTALL=ON \
    -DgRPC_BUILD_TESTS=OFF \
    -DCMAKE_INSTALL_PREFIX=/install \
    -DCMAKE_BUILD_TYPE=Release \
    -DgRPC_BUILD_GRPC_NODE_PLUGIN=OFF \
    -DgRPC_BUILD_GRPC_OBJECTIVE_C_PLUGIN=OFF \
    -DgRPC_BUILD_GRPC_PHP_PLUGIN=OFF \
    -DgRPC_BUILD_GRPC_PHP_PLUGIN=OFF \
    -DgRPC_BUILD_GRPC_PYTHON_PLUGIN=OFF \
    -DgRPC_BUILD_GRPC_RUBY_PLUGIN=OFF \
    -DCMAKE_CXX_STANDARD=17 \
    ../.. \
  && make -j2 \
  && make install

WORKDIR /opt
RUN git clone --shallow-submodules --depth 1 --recurse-submodules -b v1.8.1 \
  https://github.com/open-telemetry/opentelemetry-cpp.git \
  && cd opentelemetry-cpp \
  && mkdir build \
  && cd build \
  && cmake -DCMAKE_BUILD_TYPE=Release \
    -DCMAKE_INSTALL_PREFIX=/install \
    -DCMAKE_PREFIX_PATH=/install \
    -DWITH_OTLP=ON \
    -DWITH_OTLP_GRPC=ON \
    -DWITH_OTLP_HTTP=OFF \
    -DBUILD_TESTING=OFF \
    -DWITH_EXAMPLES=OFF \
    -DCMAKE_CXX_STANDARD=17 \
    -DCMAKE_POSITION_INDEPENDENT_CODE=ON \
    .. \
  && make -j2 \
  && make install

WORKDIR /opt
RUN set -x \
    && git clone https://github.com/open-telemetry/opentelemetry-cpp-contrib.git \
    && cd opentelemetry-cpp-contrib/instrumentation/nginx \
    && mkdir build \
    && cd build \
    && cmake \
        -DCMAKE_BUILD_TYPE=Release \
        -DCMAKE_PREFIX_PATH=/install \
        -DCMAKE_INSTALL_PREFIX=/usr/share/nginx/modules \
        -DCMAKE_POSITION_INDEPENDENT_CODE=ON \
        -DWITH_OTLP=ON \
        -DNGINX_VERSION=${NGINX_VERSION} \
        -DNGINX_CONFIGURE_ARGS="--with-compat" \
        .. \
    && make -j4 \
    && make install

FROM nginx:1.24.0
COPY --from=builder /opt/opentelemetry-cpp-contrib/instrumentation/nginx/build/otel_ngx_module.so /usr/lib/nginx/modules/otel_ngx_module.so

RUN set -x \
    && apt-get update && apt-get install -y \
        wget \
        pkg-config \
        libc-ares-dev \
        libre2-dev \
    && rm -rf /var/lib/apt/lists/*

# Use confd to render our config templates since Prometheus doesn't support env vars in their yaml
RUN mkdir -p /usr/local/bin && \
    wget -O /usr/local/bin/confd https://github.com/kelseyhightower/confd/releases/download/v0.16.0/confd-0.16.0-linux-amd64 && \
    /bin/chmod +x /usr/local/bin/confd

COPY --chmod=775 src/frontendproxy-third-party-nginx-otel/40-confd.sh /docker-entrypoint.d/40-confd.sh
COPY src/frontendproxy-third-party-nginx-otel/nginx.toml /etc/confd/conf.d/nginx.toml
COPY src/frontendproxy-third-party-nginx-otel/nginx.conf.tmpl /etc/confd/templates/nginx.conf.tmpl
