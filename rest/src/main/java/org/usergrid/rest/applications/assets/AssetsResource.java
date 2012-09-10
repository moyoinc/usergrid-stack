package org.usergrid.rest.applications.assets;

import com.sun.jersey.api.json.JSONWithPadding;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.multipart.MultiPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.usergrid.mq.QueueQuery;
import org.usergrid.mq.QueueResults;
import org.usergrid.persistence.EntityManager;
import org.usergrid.persistence.entities.Asset;
import org.usergrid.rest.applications.ApplicationResource;
import org.usergrid.rest.applications.ServiceResource;
import org.usergrid.rest.security.annotations.RequireApplicationAccess;
import org.usergrid.services.assets.data.BinaryStore;
import org.usergrid.utils.StringUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * @author zznate
 */
@Component("org.usergrid.rest.applications.assets.AssetsResource")
@Scope("prototype")
@Produces(MediaType.APPLICATION_JSON)
public class AssetsResource extends ServiceResource {

  private Logger logger = LoggerFactory.getLogger(AssetsResource.class);

  @Autowired
  private BinaryStore binaryStore;

  @Override
  @RequireApplicationAccess
  @GET
  public JSONWithPadding executeGet(@Context UriInfo ui,
                                    @QueryParam("callback") @DefaultValue("callback") String callback)
          throws Exception {
    logger.info("In AssetsResource.executeGet with ui: {} and callback: {}", ui, callback);
    return super.executeGet(ui, callback);
  }

  @Override
  @PUT
 	@RequireApplicationAccess
 	@Consumes(MediaType.APPLICATION_JSON)
 	public JSONWithPadding executePut(@Context UriInfo ui,
 			Map<String, Object> json,
 			@QueryParam("callback") @DefaultValue("callback") String callback) throws Exception {

    return super.executePut(ui, json, callback);
  }

  @POST
  @RequireApplicationAccess
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Path("{entityId: [A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}}/data")
  public Response uploadData(@FormDataParam("file") InputStream uploadedInputStream,
                                    @FormDataParam("file") FormDataContentDisposition fileDetail,
                                    @PathParam("entityId") PathSegment entityId) throws Exception {

    UUID assetId = UUID.fromString(entityId.getPath());
    logger.info("In AssetsResource.uploadData with id: {}",assetId);
    EntityManager em = emf.getEntityManager(getApplicationId());
    Asset asset = em.get(assetId, Asset.class);

    binaryStore.write(getApplicationId(), asset, uploadedInputStream);

    return Response.status(200).build();
  }

  @POST
  @RequireApplicationAccess
  @Consumes(MediaType.APPLICATION_OCTET_STREAM)
  @Path("{entityId: [A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}}/data")
  public Response uploadDataStream( @PathParam("entityId") PathSegment entityId,
                                    InputStream uploadedInputStream) throws Exception {

    UUID assetId = UUID.fromString(entityId.getPath());
    logger.info("In AssetsResource.uploadDataStream with id: {}",assetId);
    EntityManager em = emf.getEntityManager(getApplicationId());
    Asset asset = em.get(assetId, Asset.class);

    binaryStore.write(getApplicationId(), asset, uploadedInputStream);

    return Response.status(200).build();
  }

  @GET
  @Path("{entityId: [A-Fa-f0-9]{8}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{4}-[A-Fa-f0-9]{12}}/data")
  public Response findAsset(@Context UriInfo ui,
                                   @QueryParam("callback") @DefaultValue("callback") String callback,
                                   @PathParam("entityId") PathSegment entityId,
                                   @HeaderParam("range") String range)
          throws Exception {
    UUID assetId = UUID.fromString(entityId.getPath());
    logger.info("In AssetsResource.findAsset with id: {} and range: {}",assetId, range);
    EntityManager em = emf.getEntityManager(getApplicationId());

    Asset asset = em.get(assetId, Asset.class);

    InputStream is;
    if ( StringUtils.isBlank(range) ) {
      is = binaryStore.read(getApplicationId(), asset);
    } else {
      // TODO range parser
      is = binaryStore.read(getApplicationId(), asset);
    }

    return Response.ok(is)
            .type(asset.getProperty("content-type").toString())
            .lastModified(new Date(asset.getModified()))
            .build();
  }

}
