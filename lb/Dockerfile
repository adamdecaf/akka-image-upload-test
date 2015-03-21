FROM ubuntu:14.04

# Largely from: https://github.com/dockerfile/nginx/blob/master/Dockerfile
RUN \
  # add-apt-repository -y ppa:nginx/stable && \
  apt-get update && \
  apt-get install -y nginx && \
  rm -rf /var/lib/apt/lists/* && \
  echo "\ndaemon off;" >> /etc/nginx/nginx.conf && \
  chown -R www-data:www-data /var/lib/nginx

WORKDIR /etc/nginx
CMD ["nginx"]

VOLUME /var/log/nginx
VOLUME /usr/share/nginx/html/images/

EXPOSE 80
EXPOSE 443

RUN rm -f /etc/nginx/sites-enabled/default && mkdir -p /var/cache/images/data

ADD images.conf /etc/nginx/sites-enabled/images.conf
ADD nginx.conf /etc/nginx/nginx.conf
