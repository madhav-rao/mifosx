package org.mifosng.platform.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mifosng.platform.api.commands.BranchMoneyTransferCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.OfficeTransactionData;
import org.mifosng.platform.api.infrastructure.PortfolioApiDataConversionService;
import org.mifosng.platform.api.infrastructure.PortfolioApiJsonSerializerService;
import org.mifosng.platform.infrastructure.api.ApiParameterHelper;
import org.mifosng.platform.organisation.service.OfficeReadPlatformService;
import org.mifosng.platform.organisation.service.OfficeWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/officetransactions")
@Component
@Scope("singleton")
public class OfficeTransactionsApiResource {

	@Autowired
	private OfficeWritePlatformService writePlatformService;

	@Autowired
	private OfficeReadPlatformService readPlatformService;

	@Autowired
	private PortfolioApiDataConversionService apiDataConversionService;

	@Autowired
	private PortfolioApiJsonSerializerService apiJsonSerializerService;

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveOfficeTransactions(@Context final UriInfo uriInfo) {

		Set<String> typicalResponseParameters = new HashSet<String>(
				Arrays.asList("id", "transactionDate", "fromOfficeId",
						"fromOfficeName", "toOfficeId", "toOfficeIdName",
						"currencyCode", "digitsAfterDecimal",
						"transactionAmount", "description"));

		Set<String> responseParameters = ApiParameterHelper
				.extractFieldsForResponseIfProvided(uriInfo
						.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo
				.getQueryParameters());

		Collection<OfficeTransactionData> officeTransactions = this.readPlatformService
				.retrieveAllOfficeTransactions();

		return this.apiJsonSerializerService
				.serializeOfficeTransactionDataToJson(prettyPrint,
						responseParameters, officeTransactions);
	}

	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String newOfficeTransactionDetails(@Context final UriInfo uriInfo) {

		Set<String> typicalResponseParameters = new HashSet<String>(
				Arrays.asList("id", "transactionDate", "allowedOffices",
						"currencyOptions"));

		Set<String> responseParameters = ApiParameterHelper
				.extractFieldsForResponseIfProvided(uriInfo
						.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo
				.getQueryParameters());

		OfficeTransactionData officeTransactionData = this.readPlatformService
				.retrieveNewOfficeTransactionDetails();

		return this.apiJsonSerializerService
				.serializeOfficeTransactionDataToJson(prettyPrint,
						responseParameters, officeTransactionData);
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response transferMoneyFrom(final String jsonRequestBody) {

		BranchMoneyTransferCommand command = this.apiDataConversionService
				.convertJsonToBranchMoneyTransferCommand(jsonRequestBody);

		Long id = this.writePlatformService
				.externalBranchMoneyTransfer(command);
		return Response.ok().entity(new EntityIdentifier(id)).build();
	}
}