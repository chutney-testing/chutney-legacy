FROM nginx:1.17.3-alpine
ARG WEBSITE=ui/dist
COPY $WEBSITE/chutney /usr/share/nginx/html
COPY .docker/ui/nginx/nginx.conf  /etc/nginx/conf.d/default.conf
COPY .docker/ui/certif  /certif
LABEL org.opencontainers.image.source https://github.com/chutney-testing/chutney
EXPOSE 80 443
