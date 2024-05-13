FROM node:20.10.0 as build
COPY package*.json ./
RUN rm -rf node_modules/
RUN npm install
COPY . .
RUN npm run build
FROM nginx:alpine as production-stage
COPY nginx.conf /etc/nginx/conf.d/nginx.conf
COPY --from=build dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]