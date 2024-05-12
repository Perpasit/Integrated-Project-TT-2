# Build Stage
FROM node:lts-bullseye AS build

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

# Run Stage
FROM nginx:stable-alpine


COPY nginx.conf /etc/nginx/conf.d/nginx.conf

COPY --from=build dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]