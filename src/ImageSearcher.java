/**
 * Created by yudzh_000 on 08.02.2016.
 */

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.json.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageSearcher {

    public static  ImageSearcher searcher = new ImageSearcher();
    private static JSONParser parser = new JSONParser();
    public String getLogoUrl(String name) throws IOException {
        String params = (name +" " + "логотип").replaceAll(" ","%20");
        URL url = new URL("https://yandex.ru/images/search?text=" +params);
        Map<String,String> cookies = new HashMap<>();
        cookies.put("sessionid2","3:1457124735.5.0.1456660048939:X__kXg:6.1|366728260.0.2|142270.438408.UYpmnEniDiYFXMG8SyohbhbZlTk");
        cookies.put("Session_id","3:1457124735.5.0.1456660048939:X__kXg:6.0|366728260.0.2|142270.99942.tAsraewVK-P0y5jY9mE7UfErAS4");
        cookies.put("L","RQpCRX94cll7cAcAR3xGa1xtVX1cdVlUHT8PAzFbJAc=.1456660048.12253.341708.a8cba4540b4ebcc78b9a1d17c92d08b3");
        cookies.put("_ym_isad","1");
        cookies.put("yandex_login","mealsoul");
        cookies.put("_ym_uid","1457380057167915847");
        cookies.put("dps","1.1");
        cookies.put("yp","1460121511.cnps.431038718%3Amax%231772019590.multib.1%231473122264.szm.1%3A1920x1080%3A1920x947%231461418454.ww.1%231459025863.ygu.1%231772020048.udn.cDptZWFsc291bA%3D%3D%231487764220.dsws.1%231487764220.dswa.0%231487764220.dwss.1%231457599290.nps.37552912%3Aclose");
        cookies.put("zm","m-white_bender.flex.webp.css-https%3Awww_G4BDPPMDlZ8VOuW7ZiLZQUUffOE%3Al");
        cookies.put("_ym_isad","1");
        cookies.put("_ym_visorc_10632040","w");
        cookies.put("yandexuid", "4958263251456660299");
        cookies.put("_ym_visorc_10630330","w");

        Document doc = Jsoup.connect("https://yandex.ru/images/search?text="+params).cookies(cookies).get();
        Elements sorces = doc.select(".serp-list");
        if(sorces.size() == 0) {
                int i =  5;
        }
        sorces = sorces.get(0).children();
        List<Image> images = new ArrayList<>();
        for (Element sorce : sorces) {
            try {
                JSONObject obj = (JSONObject) parser.parse(sorce.attr("data-bem"));
                images.add(new Image(obj));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        for (Image image : images) {
            if (image.isSquar()) {
                return image.url;
            }
        }

        return images.get(0).url;
    }

    public  String getImageUrl(String name) throws IOException {
        String params = name.replaceAll(" ","%20") + "%20главный%20корпус" +"&isize=medium";
        URL url = new URL("https://yandex.ru/images/search?text=" +params);


        Map<String,String> cookies = new HashMap<>();
        cookies.put("sessionid2","3:1457124735.5.0.1456660048939:X__kXg:6.1|366728260.0.2|142270.438408.UYpmnEniDiYFXMG8SyohbhbZlTk");
        cookies.put("Session_id","3:1457124735.5.0.1456660048939:X__kXg:6.0|366728260.0.2|142270.99942.tAsraewVK-P0y5jY9mE7UfErAS4");
        cookies.put("L","RQpCRX94cll7cAcAR3xGa1xtVX1cdVlUHT8PAzFbJAc=.1456660048.12253.341708.a8cba4540b4ebcc78b9a1d17c92d08b3");
        cookies.put("_ym_isad","1");
        cookies.put("yandex_login","mealsoul");
        cookies.put("_ym_uid","1457380057167915847");
        cookies.put("dps","1.1");
        cookies.put("yp","1460121511.cnps.431038718%3Amax%231772019590.multib.1%231473122264.szm.1%3A1920x1080%3A1920x947%231461418454.ww.1%231459025863.ygu.1%231772020048.udn.cDptZWFsc291bA%3D%3D%231487764220.dsws.1%231487764220.dswa.0%231487764220.dwss.1%231457599290.nps.37552912%3Aclose");
        cookies.put("zm","m-white_bender.flex.webp.css-https%3Awww_G4BDPPMDlZ8VOuW7ZiLZQUUffOE%3Al");
        cookies.put("_ym_isad","1");
        cookies.put("_ym_visorc_10632040","w");
        cookies.put("yandexuid", "4958263251456660299");
        cookies.put("_ym_visorc_10630330","w");
        Document doc = Jsoup.connect("https://yandex.ru/images/search?text="+params).cookies(cookies).get();

        Elements sorces = doc.select(".serp-list");
        if(sorces.size() == 0) {
             int i =  5;
        }
        sorces = sorces.get(0).children();
        List<Image> images = new ArrayList<>();
        sorces.forEach(element -> {
            try {
                JSONObject obj =(JSONObject) parser.parse(element.attr("data-bem"));
                images.add(new Image(obj));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });

        for (Image image : images) {
            if (image.isConfim()) {
                return image.url;
            }
        }


        return images.get(0).url;
    }

    private class Image {
        String url;
        Long width;
        Long height;
        boolean isCorrect = true;
        final int DELTA = 50;

        final int MINHEIGHT = 700;
        final int MINWIDTH = 500;




        Image(JSONObject jsonObject) {
            JSONObject json = (JSONObject) jsonObject.get("serp-item");
            JSONArray values = (JSONArray) json.get("preview");
            json = (JSONObject) values.get(0);
            this.width = (Long) json.get("width");
            this.url = json.get("url").toString();
            this.height =  (Long) json.get("height");

        }




        @Override
        public String toString() {
            return "h=" + height +" w="+ width;
        }

        public boolean isConfim() {
            boolean result = true;

            if(this.height < MINHEIGHT || this.width < MINWIDTH) {
                result = false;
            }

            if(this.height > MINHEIGHT + 300 && this.width > MINWIDTH + 200) {
                result = false;
            }


            if(this.height - this.width > 250) {
                result = false;
            }
            return result && isAvailable();
        }
        private boolean isAvailable() {
            Document doc = null;
            try {
                doc = Jsoup.connect(this.url).get();
                Elements el = doc.getElementsByTag("img");
                if(el.size() == 0) {
                    return false;
                }
                return true;
            } catch (IOException e) {
                return false;
            }

        }
        public boolean isSquar() {
            return Math.abs(height - width) < DELTA;
        }
    }
}
