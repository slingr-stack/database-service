
# Overview

Repo: [https://github.com/slingr-stack/database-service](https://github.com/slingr-stack/database-service)

This service provides a robust interface for managing documents within a datastore in your SLINGR applications. It supports essential CRUD operations, including:

- **save:** Insert a new document into a specified collection.
- **findOne:** Retrieve a single document by its unique identifier.
- **findAll:** Retrieve all documents from a collection.
- **deleteOne:** Remove a specific document using its unique identifier.
- **deleteAll:** Remove all documents within a specified collection.

Each function is designed for simplicity and efficiency, ensuring seamless integration and easy testing in your Slingr environment.

# Javascript API

Below are examples of how to use each function through the Slingr Javascript API. 
Replace `svc.database` with your actual service namespace if necessary.

## Save a document

```js
log(JSON.stringify(svc.database.save({
    document: { key: "value" },
    collection: "test1" // Optional: defaults to "default" if not provided
})));
```

## Find one document

```js
log(JSON.stringify(svc.database.findOne({
    externalId: "unique-document-id",
    collection: "test1" // Optional: defaults to "default" if not provided
})));
```

## Find all documents

```js
log(JSON.stringify(svc.database.findAll({
    collection: "test1" // Optional: defaults to "default" if not provided
})));
```

## Delete one document

```js
log(JSON.stringify(svc.database.deleteOne({
    externalId: "unique-document-id",
    collection: "test1" // Optional: defaults to "default" if not provided
})));
```

## Delete all documents

```js
log(JSON.stringify(svc.database.deleteAll({
    collection: "test1" // Optional: defaults to "default" if not provided
})));
```

# About Slingr

SLINGR is a low-code rapid application development platform that accelerates development, with robust architecture for integrations and executing custom workflows and automation.

[More info about SLINGR](https://slingr.io)

# License

This service is licensed under the Apache License 2.0. See the `LICENSE` file for more details.
