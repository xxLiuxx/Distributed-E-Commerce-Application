# Shop Project

Shop Project is a self-practice online shopping system consists of a backend management system and a frontend portal system. The implementation is based on SpringCloud. In the management system, functionalities such as goods management, brand management and category management are completed. To the frontend portal, goods searching, registering, making orders and making payment are finished.

The project mainly consists of these following modules:

```
ProjectShop
├── shop-common -- utils and general usage code
├── shop-gateway -- Zuul gateway
├── shop-user -- user registeration
├── shop-item -- item service for backend management system and provides api for other services
├── shop-search -- service based on ElaticSearch for goods searching in the frontend portal
├── shop-user -- user registeration
├── shop-auth -- user validation check and token generation with JWT
├── shop-sms -- send validation code message to user's phone when the user is registering for the website
├── shop-goods-web -- generate the static page for the goods detail page
├── shop-user -- user registeration
├── shop-upload -- upload image files to the server using FastDFS
├── shop-order -- making orders
├── shop-cart -- user cart management
```

Backend Tech Stacks: 
SpringCloud, SpringBoot, MyBatis, TkMapper, ElasticSearch, Redis, RabbitMQ, Nginx, JWT, PageHelper, Druid, Thymeleaf, FastDFS

Frontend Tech Stacks: 
HTML, CSS, JavaScript, axios, Vue, vuetify, vue-router
