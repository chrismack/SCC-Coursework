/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.brokering;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 *
 * @author Chris Mack N0576565
 */
@WebService(serviceName = "ShareBrokeringWS")
@Stateless()
public class ShareBrokeringWS {

    
    // Location from which we marshal and unmarshal XML
    private final File sharesXML = new File("Shares.xml");
    private final Share errorShare = new Share();

    private static final String QUANDL = "www.quandl.com";
    private static final String QUANDLPATH = "/api/v3/datasets/";
    
    private static final String NEWS = "newsapi.org";
    private static final String NEWSPATH = "/v2/everything/";
    private static final String NEWSKEY = "5ab4f01d60db4e53a512bbc9be6b2610";
    
    public ShareBrokeringWS() {
        errorShare.setCompanyName("No shares could be found");
    }
    
    /**
     * Web service operation
     * @return 
     */
    @WebMethod(operationName = "setup")
    public Shares setup() {
        Shares shares = new Shares();
        List<Share> sharesList = shares.getShares();
        
        Share share = new Share();
        share.setCompanyName("Apple");
        share.setCompanySymobol("AAPL");
        share.setAvailableShares(100);
        
        SharePrice price = new SharePrice();
        price.setCurrency("USD");
        price.setValue(1);
        price.setLastUpdate(generateDate(2017, 12, 1));
        share.setPrice(price);
        sharesList.add(share);
        
        
        share = new Share();
        share.setCompanyName("Nvidia");
        share.setCompanySymobol("NVDA");
        share.setAvailableShares(100);
        
        price = new SharePrice();
        price.setCurrency("USD");
        price.setValue(0);
        price.setLastUpdate(generateDate(2017, 12, 1));
        share.setPrice(price);
        sharesList.add(share);
        
        marshalShares(shares);
        
        return shares;
    }

    private boolean isStringEmpty(String str) {
        return str == null || str.equalsIgnoreCase("");
    }
    
    /**
     * Web service operation
     * @param companyName
     * @param companySymbol
     * @param minShares
     * @param maxShares
     * @param currentPrice
     * @param minPrice
     * @param maxPrice
     * @return 
     */
    @WebMethod(operationName = "searchShares")
    public Shares searchShares( @WebParam(name = "companyName") String companyName, 
                                @WebParam(name = "companySymbol") String companySymbol,
                                @WebParam(name = "minShares") String minShares,
                                @WebParam(name = "maxShares") String maxShares,
                                @WebParam(name = "currentPrice") String currentPrice,
                                @WebParam(name = "minPrice") String minPrice,
                                @WebParam(name = "maxPrice") String maxPrice) {
        
        List<Share> sharesList = getAllShares();
        
        Shares foundShares = new Shares();
        List<Share> searchList = foundShares.getShares();
        
        for (Share share : sharesList) {
            if(share.getCompanyName().contains(companyName)) {
                boolean matches = false;
                
                if(!isStringEmpty(companyName)) {
                    matches = share.getCompanyName().contains(companyName);
                }
                if(!isStringEmpty(companySymbol) && matches) {
                    matches = share.getCompanySymobol().contains(companySymbol);
                }
                
                // Check min and max shares avaliable
                if(!isStringEmpty(minShares) && matches) {
                    matches = share.getAvailableShares() >= Integer.parseInt(minShares);
                }
                if(!isStringEmpty(maxShares) && matches) {
                    matches = share.getAvailableShares() <= Integer.parseInt(maxShares);
                }
                
                if(!isStringEmpty(minPrice) && matches) {
                    matches = share.getPrice().getValue() >= Float.parseFloat(minPrice);
                }
                if(!isStringEmpty(maxPrice) && matches) {
                    matches = share.getPrice().getValue() >= Float.parseFloat(maxPrice);
                }
                
                if(matches) {
                    searchList.add(share);
                }
            }
        }
        
        return foundShares;
    }
    
        /**
     * Web service operation
     * @return 
     */
    @WebMethod(operationName = "getAllShares")
    public List<Share> getAllShares() {
        Shares shares = unmarshalShares();
        List<Share> sharesList = shares.getShares();
        for(Share share : sharesList) {
            updateShare(share);
        }
        marshalShares(shares);
        return sharesList;    
    }

    /**
     * Web service operation
     * @param name
     * @return 
     */
    @WebMethod(operationName = "getShareName")
    public Share getShareByName(@WebParam(name = "companyName") String name) {
        List<Share> sharesList = getAllShares();
        
        for (Share share : sharesList) {
            if (name.equalsIgnoreCase(share.getCompanyName())) {
                return share;
            }
        }
        
        return null;
    }
    
    /**
     * Web service operation
     * @param sym
     * @return 
     */
    @WebMethod(operationName = "getShareBySymobol")
    public Share getShareBySymobol(@WebParam(name = "comanySymbol") String sym) {
        List<Share> sharesList = getAllShares();
        
        for(Share share : sharesList) {
            if(sym.equalsIgnoreCase(share.getCompanySymobol())) {
                return share;
            }
        }
        return errorShare; 
    }
    
    /**
     * Web service operation
     * @param symbol
     * @param volume
     * @return 
     */
    @WebMethod(operationName = "buyShares")
    public String buyShares(@WebParam(name = "symbol") String symbol, 
                            @WebParam(name = "volume") int volume) {
        if(volume > 0) {
            List<Share> sharesList = getAllShares();
            for(Share share : sharesList) {
                if(share.getCompanySymobol().equalsIgnoreCase(symbol)) {
                    int currentShares = share.getAvailableShares();
                    if(currentShares >= volume) {
                        share.setAvailableShares(currentShares - volume);
                        
                        Shares shares = new Shares();
                        shares.shares = sharesList;
                        marshalShares(shares);
                        return volume + " shares bought from " + symbol;
                    }
                }
            }
        }
        
        return "Error buying " + volume + "shares from " + symbol;
    }
    
    private InputStream sendGet(URL url, Map<String, String> headers) {
        try {
            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            if(headers != null) {
                Iterator it = headers.entrySet().iterator();
                while(it.hasNext()) {
                    Map.Entry<String, String> pair = (Map.Entry) it.next();
                    connection.setRequestProperty(pair.getKey(), pair.getValue());
                    it.remove();
                }
            }
            
            int responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
                return connection.getInputStream();
            }
            
        } catch (IOException ex) {
            Logger.getLogger(ShareBrokeringWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private String queryBuilder(Map<String, String> queryMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("?");
        Iterator it = queryMap.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, String> pair = (Map.Entry) it.next();
            sb.append(pair.getKey());
            sb.append("=");
            sb.append(pair.getValue());
            if(it.hasNext()) {
                sb.append("&");
            }
        }
        return sb.toString();
    }
    
    private void updateShare(Share share) {
        
        try {
            // TODO update the share
            // Update Value
            // Update History
            String urlString = "https://" + QUANDL + QUANDLPATH + "WIKI/" + share.companySymobol + ".json";
            
            Map<String, String> queries = new HashMap<>();
            queries.put("rows", "8");
//            queries.put("column_index", "4");
            urlString += queryBuilder(queries);
            
            URL url = new URL(urlString);
            InputStream is = sendGet(url, null);
            
            if(is != null) {
                JsonReader json = Json.createReader(is);
                JsonObject dataset = json.readObject().getJsonObject("dataset");
                JsonArray data = dataset.getJsonArray("data");
                
                
                // Update the current value of the share
                JsonArray todaysData = data.getJsonArray(0);
                share.getPrice().setValue(Float.parseFloat(todaysData.get(1).toString()));
                
                // Update previous prices for share
                List<ShareHistory> historyList = new ArrayList<>();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                
                for(int i = 1; i <= 7; i++) {
                    JsonArray pastValues = data.getJsonArray(i);
                    ShareHistory history = new ShareHistory();
                    
                    Date date = dateFormat.parse(pastValues.getString(0));
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    
                    float open = Float.parseFloat(pastValues.get(1).toString());
                    float high = Float.parseFloat(pastValues.get(2).toString());
                    float low = Float.parseFloat(pastValues.get(3).toString());
                    float close = Float.parseFloat(pastValues.get(4).toString());
                    
                    history.setDate(generateDate(cal.get(Calendar.YEAR), 
                        cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)));
                    history.setOpen(open);
                    history.setClose(close);
                    history.setLow(low);
                    history.setHigh(high);
                    historyList.add(history);
                }
                share.getPrice().history = historyList;
                
            }
        } catch (IOException | ParseException ex) {
            Logger.getLogger(ShareBrokeringWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Calendar cal = Calendar.getInstance();
        share.getPrice().setLastUpdate(generateDate(cal.get(Calendar.YEAR), 
                cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)));
        
    }
    
    private XMLGregorianCalendar generateDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        XMLGregorianCalendar xmlDate;
        GregorianCalendar data = new GregorianCalendar(year, month, day, 
                cal.get(Calendar.HOUR), 
                cal.get(Calendar.MINUTE), 
                cal.get(Calendar.SECOND));
        try {
            xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(data);
            return xmlDate;
        } catch (DatatypeConfigurationException e) {
            // TODO: replace with logger
            System.out.println(e);
        }
        return null;
    }
    
    private void marshalShares(Shares shares){
        try {
            javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(Shares.class);
            javax.xml.bind.Marshaller marshaller = jaxbCtx.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8"); //NOI18N
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(shares, sharesXML);
        } catch (JAXBException e) {
            System.out.println(e);
        }   
    }
    
    private Shares unmarshalShares() {
        try {
            javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(Shares.class);
            javax.xml.bind.Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
            Shares shares = (Shares) unmarshaller.unmarshal(sharesXML); //NOI18N
            return shares;
        } catch (JAXBException e) {
            System.out.println(e);
        }
        return null;
    }

    /**
     * Web service operation
     * @param symbol
     * @return 
     */
    @WebMethod(operationName = "getShareNews")
    public List<ShareNews> getShareNews(@WebParam(name = "symbol") String symbol) {
        //TODO write your implementation code here:
        if(symbol != null) {
            String newsURL = "https://" + NEWS + NEWSPATH;
            Map<String, String> queries = new HashMap<>();
            queries.put("q", symbol);
            queries.put("sortBy", "publishedAt");
            queries.put("apiKey", NEWSKEY);
            queries.put("language", "en");
            newsURL += queryBuilder(queries);
            
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json; charset-utf8");
            headers.put("Accept-Charset", "utf-8");
            
            List<ShareNews> newsList = new ArrayList<>();
            
            try {
                InputStream is = sendGet(new URL(newsURL), headers);
                if(is != null) {
                    JsonReader json = Json.createReader(is);
                    JsonObject jo = json.readObject();
                    JsonArray jArr = jo.getJsonArray("articles");
                    for(int i = 0; i < jArr.size(); i++) {
                        ShareNews news = new ShareNews();
                        
                        JsonObject currentItem = jArr.getJsonObject(i);
                        
                        String source = currentItem.getJsonObject("source").getString("name");
                        String title = currentItem.getString("title");
                        String author = currentItem.getString("author", "");
                        String description = currentItem.getString("description");
                        String url = currentItem.getString("url");
                        
                        news.setSource(source);
                        news.setTitle(title);
                        news.setAuthor(author);
                        news.setDescription(description);
                        news.setUrl(url);
                        newsList.add(news);
                    }
                    return newsList;
                } 
            } catch (MalformedURLException ex) {
                Logger.getLogger(ShareBrokeringWS.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return null;
    }

}
