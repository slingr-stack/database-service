package io.slingr.services.database;

import io.slingr.services.Service;
import io.slingr.services.framework.annotations.*;
import io.slingr.services.services.AppLogs;
import io.slingr.services.services.datastores.DataStore;
import io.slingr.services.services.datastores.DataStoreResponse;
import io.slingr.services.utils.Json;
import io.slingr.services.utils.Strings;
import io.slingr.services.ws.exchange.FunctionRequest;
import io.slingr.services.framework.annotations.ApplicationLogger;
import io.slingr.services.framework.annotations.SlingrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service used as a proxy to services on the developer environment
 * <p>
 * Created by agreggio on 23/10/23.
 */
@SlingrService(name = "database")
public class Database extends Service {
    private static final String SERVICE_NAME = "database";
    private static final Logger logger = LoggerFactory.getLogger(Database.class);

    private static final String ID = "_id";
    private static final String APP = "app";
    private static final String ENV = "env";
    private static final String EXTERNAL_ID = "externalId";
    private static final String DOCUMENT = "document";
    private static final String COLLECTION = "collection";
    private static final String DEFAULT_COLLECTION = "default";

    @ApplicationLogger
    protected AppLogs appLogs;

    @ServiceDataStore(name = "datastore")
    private DataStore datastore;

    public void serviceStarted() {
        logger.info(String.format("Initializing service [%s]", SERVICE_NAME));
        appLogs.info(String.format("Initializing service [%s]", SERVICE_NAME));
    }

    @ServiceFunction(name = "save")
    public Json save(FunctionRequest request) {
        logger.info("Save received");
        final Json data = request.getJsonParams();
        String document = data.string(DOCUMENT);
        String collection = data.contains(COLLECTION) ? data.string(COLLECTION) : DEFAULT_COLLECTION;

        final Json documentJson = Json.map()
                .set(ID, Strings.randomUUIDString())
                .set(DOCUMENT, document)
                .set(COLLECTION, collection);
        final Json saved = datastore.save(documentJson);
        Database.formatRecord(saved, documentJson);
        return saved;
    }

    @ServiceFunction(name = "findOne")
    public Json findOne(FunctionRequest request) {
        logger.info("findOne received");
        final Json query = Database.formatRequest(request);
        DataStoreResponse result = datastore.find(query);
        if (result != null && result.getTotal() > 0) {
            Database.formatRecord(result.getItems().get(0), query);
            return result.getItems().get(0);
        }
        return Json.map();
    }

    @ServiceFunction(name = "findAll")
    public Json findAll(FunctionRequest request) {
        logger.info("findAll received");
        Json params = request.getJsonParams();
        String collection = params.contains(COLLECTION) ? params.string(COLLECTION) : DEFAULT_COLLECTION;
        Json query = Json.map().set(COLLECTION, collection);
        DataStoreResponse result = datastore.find(query);
        if (result != null) {
            for (Json item : result.getItems()) {
                Database.formatRecord(item, item);
            }
            return Json.map()
                    .set("total", result.getTotal())
                    .set("items", result.getItems());
        }
        return Json.map().set("total", 0).set("items", Json.list());
    }

    @ServiceFunction(name = "deleteOne")
    public Json deleteOne(FunctionRequest request) {
        logger.info("deleteOne received");
        final Json found = findOne(request);
        if (found.isEmpty()) {
            return Json.map().set("message", "No document found to delete");
        }
        final Json query = Database.formatRequest(request);
        boolean result = datastore.remove(query);
        if (result) {
            return Json.map().set("message", "Document deleted successfully");
        }
        return Json.map().set("message", "No document found to delete");
    }

    @ServiceFunction(name = "deleteAll")
    public Json deleteAll(FunctionRequest request) {
        logger.info("deleteAll received");
        Json params = request.getJsonParams();
        String collection = params.contains(COLLECTION) ? params.string(COLLECTION) : DEFAULT_COLLECTION;
        Json query = Json.map().set(COLLECTION, collection);
        boolean result = datastore.remove(query);
        if (result) {
            return Json.map().set("message", "Documents deleted successfully");
        }
        return Json.map().set("message", "No documents found to delete");
    }

    @ServiceFunction(name = "count")
    public Json count(FunctionRequest request) {
        logger.info("count received");
        final Json params = request.getJsonParams();
        if (!params.contains(EXTERNAL_ID)) {
            return Json.map().set("error", "Parameter 'externalId' is required");
        }
        String externalId = params.string(EXTERNAL_ID);
        String collection = params.contains(COLLECTION) ? params.string(COLLECTION) : DEFAULT_COLLECTION;
        Json filter = Json.map().set(EXTERNAL_ID, externalId).set(COLLECTION, collection);
        try {
            int total = datastore.count(filter);
            return Json.map().set("total", total);
        } catch (Exception ex) {
            return Json.map().set("error", ex.getMessage());
        }
    }

    @ServiceFunction(name = "update")
    public Json update(FunctionRequest request) {
        logger.info("update received");
        final Json params = request.getJsonParams();
        if (!params.contains(EXTERNAL_ID) || !params.contains("update")) {
            return Json.map().set("error", "Parameters 'externalId' and 'update' are required");
        }
        String externalId = params.string(EXTERNAL_ID);
        String collection = params.contains(COLLECTION) ? params.string(COLLECTION) : DEFAULT_COLLECTION;
        Json query = Json.map().set(EXTERNAL_ID, externalId).set(COLLECTION, collection);
        DataStoreResponse result = datastore.find(query);
        if (result == null || result.getTotal() == 0) {
            return Json.map().set("message", "No document found matching externalId");
        }
        Json documentToUpdate = result.getItems().get(0);
        Json updateData = params.json("update");
        for (String key : updateData.keys()) {
            documentToUpdate.set(key, updateData.string(key));
        }
        Json updated = datastore.update(externalId, documentToUpdate);
        formatRecord(updated, updated);
        return updated;
    }

    private static Json formatRequest(FunctionRequest request) {
        Json params = request.getJsonParams();
        if (!params.contains(EXTERNAL_ID)) {
            return Json.map().set("error", "Parameter 'externalId' is required");
        }
        String id = params.string(EXTERNAL_ID);
        String collection = params.contains(COLLECTION) ? params.string(COLLECTION) : DEFAULT_COLLECTION;
        return Json.map()
                .set(EXTERNAL_ID, id)
                .set(COLLECTION, collection);
    }

    private static void formatRecord(Json saved, Json documentJson) {
        saved.set(EXTERNAL_ID, documentJson.string(ID));
        saved.remove(ID);
        saved.remove(APP);
        saved.remove(ENV);
    }
}