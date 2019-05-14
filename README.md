The task consist in modeling a shopping website. In the website there are users who have all the data that a person has an additionally a bank account and an email. The email should be unique and it is used for authenticating a user.

The website offers a lot of products (like Amazon), every product has an identifier, price and description, there is a limited number of items of every product.

The customers of the Website can select the products and put them in a shopping card, when they have finished they go to the shopping card and starts the checkout process, which consists in selecting a delivery address and at the end they get an order id.

In order to accomplish the task the following requirements should be fulfilled:

- The programming language should be Scala
- The backend for the Website described above should be developed
- The backend should offer the end points (REST API) to add new user, add products, put items in the shopping card and checkout the items

Constraints:
- Please use git and share the project as a git repository.
- For the task it is not necessary to create a frontend (User Interface)
- It is not necessary to have a database. It is ok to have the data in memory and they will be lost when the server is restarted.

How to run:

$ sbt run

1.Add Product

$curl -H "Content-Type: application/json" -X POST -d '{"name": "Product", "cost": 56}' http://localhost:8585/product/

{"name":"Product","cost":56,"id":1}

2.Add User

$curl -H "Content-Type: application/json" -X POST -d '{"name": "Name", "email": "email@email.com", "bankAccount": 6784}' http://localhost:8585/user/

{"name":"Name","email":"email@email.com","bankAccount":6784,"id":1}

3.Add Shopping Card

$curl -H "Content-Type: application/json" -X POST -d '{"userId": 1}' http://localhost:8585/shoppingCard/

{"userId":1,"products":[],"id":1,"checkout":false}

4.Add Product to Shopping Card
$curl -H "Content-Type: application/json" -X POST -d '{"productId": 1}' http://localhost:8585/shoppingCard/addproduct/1

{"userId":1,"products":[{"name":"Product","cost":56,"id":1}],"id":1,"checkout":false}

5.CheckOut  Shopping Card
$curl -H "Content-Type: application/json" -X POST -d '{"address": "address"}' http://localhost:8585/shoppingCard/checkout/1

{"orderId":9999,"card":{"userId":1,"products":[{"name":"Product","cost":56,"id":1}],"id":1,"checkout":true},"address":"address"}

6.Get Shopping Card
$ curl http://localhost:8585/shoppingCard/1

{"userId":1,"products":[{"name":"Product","cost":56,"id":1}],"id":1,"checkout":true}