/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.brokering;

import docwebservices.CurrencyConversionWS_Service;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.WebServiceRef;

/**
 *
 * @author Chris Mack N0576565
 */
@WebService(serviceName = "ShareBrokeringWS")
@Stateless()
public class ShareBrokeringWS {

    @WebServiceRef(wsdlLocation = "WEB-INF/wsdl/localhost_8080/CurrencyConvertor/CurrencyConversionWS.wsdl")
    private CurrencyConversionWS_Service service;

    // Location from which we marshal and unmarshal XML
    private final File sharesXML = new File("Shares.xml");
    private final Share errorShare = new Share();

    private static final String QUANDL = "www.quandl.com";
    private static final String QUANDLPATH = "/api/v3/datasets/";

    private static final String NEWS = "newsapi.org";
    private static final String NEWSPATH = "/v2/everything/";
    private static final String NEWSKEY = "5ab4f01d60db4e53a512bbc9be6b2610";

    private static final String CLEARBIT = "company.clearbit.com";
    private static final String CLEARBITPATH = "/v2/companies/find";
    private static final String CLEARBITKEY = System.getenv("CLEARBIT_KEY");
    
    // Last time the conversions were updated. Updated are done around 4pm CET
    private LocalDateTime lastUpdated = null;

    public ShareBrokeringWS() {
        errorShare.setCompanyName("No shares could be found");
    }

    /**
     * Web service operation
     *
     * @return
     */
    @WebMethod(operationName = "setup")
    public Shares setup() {
        Shares shares = new Shares();
        List<Share> sharesList = shares.getShares();

        Share share = new Share();
        share.setCompanyName("Apple");
        share.setCompanySymobol("AAPL");
        share.setDomain("apple.com");
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
        share.setDomain("nvidia.com");
        share.setAvailableShares(100);

        price = new SharePrice();
        price.setCurrency("USD");
        price.setValue(0);
        price.setLastUpdate(generateDate(2017, 12, 1));
        share.setPrice(price);
        sharesList.add(share);

        share = new Share();
        share.setCompanyName("Google");
        share.setCompanySymobol("GOOG");
        share.setDomain("google.com");
        share.setAvailableShares(1000);

        price = new SharePrice();
        price.setCurrency("USD");
        price.setValue(0);
        price.setLastUpdate(generateDate(2017, 12, 15));
        share.setPrice(price);
        sharesList.add(share);

        share = new Share();
        share.setCompanyName("Intel");
        share.setCompanySymobol("INTX");
        share.setDomain("intel.com");
        share.setAvailableShares(1000);

        price = new SharePrice();
        price.setCurrency("USD");
        price.setValue(0);
        price.setLastUpdate(generateDate(2017, 12, 15));
        share.setPrice(price);
        sharesList.add(share);

        marshalShares(shares);

        return shares;
    }

    private boolean isStringEmpty(String str) {
        return str == null || str.equalsIgnoreCase("");
    }

    private boolean containsIgnoreCase(String str1, String str2) {
        return str1.toLowerCase().contains(str2.toLowerCase());
    }

    /**
     * Web service operation
     *
     * @param companyName
     * @param companySymbol
     * @param minShares
     * @param maxShares
     * @param currentPrice
     * @param minPrice
     * @param maxPrice
     * @param currency
     * @return
     */
    @WebMethod(operationName = "searchShares")
    public Shares searchShares(@WebParam(name = "companyName") String companyName,
            @WebParam(name = "companySymbol") String companySymbol,
            @WebParam(name = "minShares") String minShares,
            @WebParam(name = "maxShares") String maxShares,
            @WebParam(name = "currentPrice") String currentPrice,
            @WebParam(name = "minPrice") String minPrice,
            @WebParam(name = "maxPrice") String maxPrice,
            @WebParam(name = "currency") String currency) {

        List<Share> sharesList = getAllShares(currency);

        Shares foundShares = new Shares();
        List<Share> searchList = foundShares.getShares();

        if(isStringEmpty(companyName + companySymbol + minShares + maxShares + minPrice + maxPrice)) {
            System.out.println(foundShares);
           foundShares.shares = sharesList;
           return foundShares;
        }
        
        for (Share share : sharesList) {
            
            boolean matches = false;

            if (!isStringEmpty(companyName)) {
                matches = containsIgnoreCase(share.getCompanyName(), companyName);
            }
            if (!isStringEmpty(companySymbol) && !matches) {
                matches = containsIgnoreCase(share.getCompanySymobol(), companySymbol);
            }

            // Check min and max shares avaliable
            if (!isStringEmpty(minShares) && !matches) {
                matches = share.getAvailableShares() >= Integer.parseInt(minShares);
            }
            if (!isStringEmpty(maxShares) && !matches) {
                matches = share.getAvailableShares() <= Integer.parseInt(maxShares);
            }

            if (!isStringEmpty(minPrice) && !matches) {
                matches = share.getPrice().getValue() >= Float.parseFloat(minPrice);
            }
            if (!isStringEmpty(maxPrice) && !matches) {
                matches = share.getPrice().getValue() >= Float.parseFloat(maxPrice);
            }
            
            if(!isStringEmpty(currentPrice) && !matches) {
                matches = share.getPrice().getValue() == Float.parseFloat(currentPrice);
            }

            if (matches) {
                searchList.add(share);
            }
        }
        

        return foundShares;
    }

    /**
     * Web service operation
     *
     * @return
     */
    @WebMethod(operationName = "getAllShares")
    public List<Share> getAllShares(@WebParam(name = "currency") String currency) {
        Shares shares = unmarshalShares();
        List<Share> sharesList = shares.getShares();
        
        if(shouldUpdate()) {
            for (Share share : sharesList) {
                updateShare(share);
                share.companyInfo = getCompanyInfo(share);
            }
            marshalShares(shares);
        }
        
        if(currency != null && !currency.equalsIgnoreCase("")) {
            docwebservices.CurrencyConversionWS port = service.getCurrencyConversionWSPort();
            for(Share share : sharesList) {
                double conversionRate = port.getConversionRate(share.getPrice().getCurrency(), currency);
                
                List<ShareHistory> histList = new ArrayList<>();
                for(ShareHistory hist : share.getPrice().getHistory()) {
                    hist.setOpen((float) (hist.getOpen() * conversionRate));
                    hist.setClose((float) (hist.getClose() * conversionRate));
                    hist.setHigh((float) (hist.getHigh() * conversionRate));
                    hist.setLow((float) (hist.getLow() * conversionRate));
                    histList.add(hist);
                }
                share.getPrice().history = histList;
                
                share.getPrice().setValue((float) (share.getPrice().getValue() * conversionRate));
                share.getPrice().setCurrency(currency);
            }
        }
        
        return sharesList;
    }

    /**
     * Web service operation
     *
     * @param name
     * @return
     */
    @WebMethod(operationName = "getShareName")
    public Share getShareByName(@WebParam(name = "companyName") String name, @WebParam(name = "currency") String currency) {
        List<Share> sharesList = getAllShares(currency);

        for (Share share : sharesList) {
            if (name.equalsIgnoreCase(share.getCompanyName())) {
                return share;
            }
        }

        return null;
    }

    /**
     * Web service operation
     *
     * @param sym
     * @return
     */
    @WebMethod(operationName = "getShareBySymobol")
    public Share getShareBySymobol(@WebParam(name = "comanySymbol") String sym, @WebParam(name = "currency") String currency) {
        List<Share> sharesList = getAllShares(currency);

        for (Share share : sharesList) {
            if (sym.equalsIgnoreCase(share.getCompanySymobol())) {
                return share;
            }
        }
        return errorShare;
    }

    /**
     * Web service operation
     *
     * @param symbol
     * @param volume
     * @return
     */
    @WebMethod(operationName = "buyShares")
    public String buyShares(@WebParam(name = "symbol") String symbol,
            @WebParam(name = "volume") int volume,
            @WebParam(name = "currency") String currency) {
        if (volume > 0) {
            List<Share> sharesList = getAllShares(currency);
            for (Share share : sharesList) {
                if (share.getCompanySymobol().equalsIgnoreCase(symbol)) {
                    int currentShares = share.getAvailableShares();
                    if (currentShares >= volume) {
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

            if (headers != null) {
                Iterator it = headers.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, String> pair = (Map.Entry) it.next();
                    connection.setRequestProperty(pair.getKey(), pair.getValue());
                    it.remove();
                }
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
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
        while (it.hasNext()) {
            Map.Entry<String, String> pair = (Map.Entry) it.next();
            sb.append(pair.getKey());
            sb.append("=");
            sb.append(pair.getValue());
            if (it.hasNext()) {
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
            queries.put("api_key", "gL4t7zbiG_3nhAHxURx2");
//            queries.put("column_index", "4");
            urlString += queryBuilder(queries);

            URL url = new URL(urlString);
            InputStream is = sendGet(url, null);

            if (is != null) {
                JsonReader json = Json.createReader(is);
                JsonObject dataset = json.readObject().getJsonObject("dataset");
                JsonArray data = dataset.getJsonArray("data");

                // Update the current value of the share
                JsonArray todaysData = data.getJsonArray(0);
                share.getPrice().setValue(Float.parseFloat(todaysData.get(1).toString()));

                // Update previous prices for share
                List<ShareHistory> historyList = new ArrayList<>();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                for (int i = 1; i <= 7; i++) {
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
        lastUpdated = LocalDateTime.now(ZoneId.of("CET"));
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

    private void marshalShares(Shares shares) {
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
     *
     * @param symbol
     * @return
     */
    @WebMethod(operationName = "getShareNews")
    public List<ShareNews> getShareNews(@WebParam(name = "symbol") String symbol) {
        //TODO write your implementation code here:
        if (symbol != null) {
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
                if (is != null) {
                    JsonReader json = Json.createReader(is);
                    JsonObject jo = json.readObject();
                    JsonArray jArr = jo.getJsonArray("articles");
                    for (int i = 0; i < jArr.size(); i++) {
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

    /**
     * Web service operation
     *
     * @return
     */
    @WebMethod(operationName = "getCompanyInfo")
    public CompanyInfo getCompanyInfo(@WebParam(name = "companySymbol") Share share) {
        CompanyInfo info = new CompanyInfo();
        if (share.getCompanySymobol() != null) {
            List<Share> sharesList = unmarshalShares().getShares();
            Share l_share = null;
            int index = 0;
            for (index = 0; index < sharesList.size(); index++) {
                l_share = sharesList.get(index);
                if (l_share.getCompanySymobol().equalsIgnoreCase(share.getCompanySymobol())) {
                    l_share = sharesList.get(index);
                    break;
                }
            }

            if (l_share != null) {
                if (l_share.getCompanyInfo() == null || l_share.getCompanyInfo().getDescription() == null) {
                    
                    String domain = l_share.getDomain();
                    try {
                        String cbURL = "https://" + CLEARBIT + CLEARBITPATH;
                        Map<String, String> queries = new HashMap<>();
                        queries.put("domain", domain);
                        cbURL += queryBuilder(queries);

                        Map<String, String> headers = new HashMap<>();
                        String key = CLEARBITKEY + ":";
                        String encodedKey = DatatypeConverter.printBase64Binary(key.getBytes("UTF-8"));
                        headers.put("Authorization", "Basic " + encodedKey);

                        InputStream is = sendGet(new URL(cbURL), headers);
                        if (is != null) {
                            JsonReader json = Json.createReader(is);
                            JsonObject jo = json.readObject();
                            info.setLegalName(jo.getString("legalName", ""));
                            info.setDescription(jo.getString("description", ""));
                            info.setCategory(jo.getJsonObject("category").getString("sector", ""));
                            info.setCountry(jo.getJsonObject("geo").getString("country", ""));

                            // save company info so we dont need to go to the rest
                            // api for every request
//                            l_share.setCompanyInfo(info);
//                            sharesList.set(index, share);
//                            Shares shares = new Shares();
//                            shares.shares = sharesList;
//                            marshalShares(shares);
                            return info;
                        }
                    } catch (UnsupportedEncodingException | MalformedURLException ex) {
                        Logger.getLogger(ShareBrokeringWS.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    return share.getCompanyInfo();
                }
            }
        }

        // should return empty object
        return info;
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "getCurrencies")
    public List<String> getCurrencies() {
        docwebservices.CurrencyConversionWS port = service.getCurrencyConversionWSPort();
        return port.getCurrencyCodes();
    }
    
    private boolean shouldUpdate() {
        if (lastUpdated == null) {
            return true;
        }

        // Check for update if last update is older than 24 hours or 
        // if this is first update after 5pm cet time today
        LocalDateTime cetTime = LocalDateTime.now(ZoneId.of("CET"));
        long hoursDiff = lastUpdated.until(cetTime, ChronoUnit.HOURS);

        return hoursDiff >= 24 || lastUpdated.getHour() < 17;
    }

}
