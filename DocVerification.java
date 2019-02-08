/***************************************************************************
 *	NEWGEN SOFTWARE TECHNOLOGIES LIMITED


 *	Group					: AP2
 *	Product / Project       : MOTC_CCM_Implementation
 *	Module					: MOTCDocVerificationRestWs
 *	File Name				: DocVerificationProp
 *	Author					: Aqib Jamal
 *	Date written(DD/MM/YYYY): 1/02/2018
 *	Purpose					: It reads the input json file and proceed to verifying 
							  the authenticity of the PDF based on whether its digital or scanned
 *****************************************************************************
 *	Change History:
 *****************************************************************************
 *	 SNo	Date	Change By		Change Description (Bug No. If Any) 
 *****************************************************************************/
package com.newgen.DocVerification;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.newgen.DocVerification.webservice.xsd.DocumentVerificationInputVO;
import com.newgen.DocVerification.webservice.xsd.DocumentVerificationOutputVO;
import com.newgen.util.DocVerificationProp;
import com.newgen.util.Global;
import com.newgen.util.StringUtil;

import Decoder.BASE64Decoder;

@Path("/verify")
public class DocVerification
{
	private static Logger logger = Logger.getLogger(DocVerification.class);

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response verifyDoc(String inputJson)
	{
		System.out.println("Inside DocVerification.verifyDoc()");
		DocumentVerificationInputVO inputVoObj = new DocumentVerificationInputVO();
		JSONParser parse = new JSONParser();
		String filePath, UploadedPDF = null;
		DocumentVerificationOutputVO outputVoObj = null;
		logger.info("The input json string is : " + inputJson);
		JSONObject jObj = null;
		try
		{
			/* Reading input JSON */;
			jObj = (JSONObject) parse.parse(inputJson);
			String base64data = (String) jObj.get("data");
			inputVoObj.setBase64Data(base64data);
			inputVoObj.setDocType((String) jObj.get("docType"));
			inputVoObj.setSource((String) jObj.get("source"));
			inputVoObj.setFileName((String) jObj.get("fileName"));
			inputVoObj.setLanguage((String) jObj.get("lang"));
			
	
			/* Reading input JSON */

			java.sql.Date date1 = new java.sql.Date((new Date()).getTime());

			SimpleDateFormat formatNowDay = new SimpleDateFormat("dd");
			SimpleDateFormat formatNowMonth = new SimpleDateFormat("MMM");
			SimpleDateFormat formatNowYear = new SimpleDateFormat("YYYY");
			SimpleDateFormat formatNowHour = new SimpleDateFormat("HH");

			String currentDay = formatNowDay.format(date1);
			String currentMonth = formatNowMonth.format(date1);
			String currentYear = formatNowYear.format(date1);
			String currentHour = formatNowHour.format(date1);
			filePath = DocVerificationProp.getProperty("filepath").trim();
			filePath = filePath + currentYear + "/" + currentMonth + "/" + currentDay + "/" + currentHour + "/";
			logger.info("The file path for upload and archived is :" + filePath);
			Files.createDirectories(Paths.get(filePath));
			Global.setuploadedFilePath(filePath);
			Global.setarchivedFilePath(filePath);
			UploadedPDF = filePath + inputVoObj.getFileName();
			File file = new File(UploadedPDF);

			/* Converting base64 to pdf */
			BASE64Decoder decoder = new BASE64Decoder();
			byte[] decodedBytes = null;
			//If bas64 pdf input data is not blank or empty
			if (base64data!=null && base64data.length() > 0)
				decodedBytes = decoder.decodeBuffer(base64data);
			else
			{
				logger.debug("Data field is blank for input json");
				outputVoObj = new DocumentVerificationOutputVO();
				outputVoObj.setMessage("Data tag cant be blank for input json request");
				outputVoObj.setStatus("1017");
				return Response.status(200).entity(outputVoObj).build();
			}

			FileOutputStream fop = new FileOutputStream(file);
			fop.write(decodedBytes);
			fop.flush();
			fop.close();
			/* Converting base64 to pdf */

			if ("Digital".equalsIgnoreCase(inputVoObj.getDocType()))
			{
				logger.debug("going for Digital doc verfication");
				DocVerificationDigitalOnline digitalobj = new DocVerificationDigitalOnline();
				outputVoObj = digitalobj.verify(inputVoObj);
				return Response.status(200).entity(outputVoObj).build();
			}
			else
			{
				logger.debug("going for scanned doc verfication");
				DocVerificationScannedOnline scannedObj = new DocVerificationScannedOnline();
				outputVoObj = scannedObj.verify(inputVoObj);
				return Response.status(200).entity(outputVoObj).build();
			}
		}
		catch (ParseException e)
		{
			logger.debug("Inside catch ParseException of DocVerification.verifyDoc()");
			outputVoObj = new DocumentVerificationOutputVO();
			outputVoObj.setMessage("Error while parsing input json obj");
			outputVoObj.setStatus("1015");
			String stackTrace = StringUtil.convertStackTraceInString(e);
			logger.error(" EXCEPTION STACK TRACE:{}" + stackTrace);
			return Response.status(200).entity(outputVoObj).build();
		}
		catch (IOException e)
		{
			logger.debug("Inside catch IOException of DocVerification.verifyDoc()");
			outputVoObj = new DocumentVerificationOutputVO();
			outputVoObj.setMessage("Error while converting base64 to PDF");
			outputVoObj.setStatus("1015");
			String stackTrace = StringUtil.convertStackTraceInString(e);
			logger.error(" EXCEPTION STACK TRACE:{}" + stackTrace);
			return Response.status(200).entity(outputVoObj).build();
		}
	}

}
