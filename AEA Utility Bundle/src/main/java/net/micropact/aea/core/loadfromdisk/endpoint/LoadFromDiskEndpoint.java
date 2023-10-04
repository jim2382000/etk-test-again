package net.micropact.aea.core.loadfromdisk.endpoint;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.entellitrak.configuration.ConfigSource;
import com.entellitrak.configuration.SystemConfiguration;
import com.entellitrak.endpoints.AbstractEndpoint;
import com.entellitrak.endpoints.EndpointExecutionContext;
import com.entellitrak.endpoints.EndpointHandler;
import com.entellitrak.handler.HandlerScript;

import net.entellitrak.aea.gl.api.etk.EndpointUtil;

@HandlerScript(type = EndpointHandler.class)
@Path("net/micropact/aea/core/loadfromdisk/endpoint/LoadFromDiskEndpoint")
public class LoadFromDiskEndpoint extends AbstractEndpoint {

	@GET
	@Path("getSystemConfig")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSystemConfig() {
		final EndpointExecutionContext etk = getExecutionContext();
		final SystemConfiguration systemConfiguration = etk.getSystemConfiguration();

		return EndpointUtil.setPublicResourceCache(etk,
				Response.ok(Map.of("isLoadFromDisk", ConfigSource.DISK == systemConfiguration.getConfigSource())))
				.build();
	}
}
