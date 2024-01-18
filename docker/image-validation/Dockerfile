FROM node:21
#
# When we build the image, we copy the executable jar in the image file system. 
#
COPY src /opt/main.java.app/

WORKDIR /opt/main.java.app/
RUN npm install

ENTRYPOINT ["node", "/opt/main.java.app/main.java.app.js"]
