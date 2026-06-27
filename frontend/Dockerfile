FROM node:22-alpine AS build
WORKDIR /workspace
COPY frontend/package*.json ./
RUN npm ci --prefer-offline
COPY frontend .
RUN npm run build

FROM nginx:1.27-alpine
COPY --from=build /workspace/dist /usr/share/nginx/html
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
