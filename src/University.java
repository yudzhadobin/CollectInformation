import com.sun.javaws.exceptions.InvalidArgumentException;
import com.sun.xml.internal.ws.api.message.ExceptionHasMessage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import javax.print.Doc;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yudzh_000 on 03.01.2016.
 */
public class University implements ConvertableToJson{

    String name;
    String url;
    String town;

    String phone;
    String fullAddress;
    String siteUrl;
    String imageUrl;
    String logoUrl;
    List<Special> specials = new ArrayList<Special>();

    static  int numb = 1;
    public static void parseFromHtml(Region region) throws IOException {
        String base = "http://www.edu.ru";

        String getRequest = "?sort=&nr=&show_results=&rgn=";

        int curPag = 1;
        String id = getId(region.url);
        int prev;
        do {
            prev = region.universities.size();
            parse(new URL(base + region.url + getRequest + id + "&spe=&page=" + Integer.toString(curPag)), region);
            curPag++;
        } while (prev != region.universities.size());

        System.out.println(region.name + " parsed " + "№" + numb);
        numb++;

    }

    private static  String getId(String url) {
        String id = "";

        for (int i = url.indexOf("rgn.") + 4; i < url.length(); i++) {
            if (url.charAt(i) == '/') {
                break;
            } else {
                id += url.charAt(i);
            }
        }
        return id;
    }


    private static void parse(URL url, Region region) throws IOException {
        Document doc = Jsoup.parse(url, 10000);

        Elements universities = doc.select("table[cellpadding=3]").select("a[href]");
        universities.forEach(element -> {
            University university = new University();
            university.name = element.childNode(0).toString().replaceAll("&nbsp;", "");
            university.url = element.attr("href");
            try {
                parseSubInfo(university);
                parseScecialTraining(university);
            } catch (Exception e) {
                int i = 5;
            }

            boolean flag = false;
            region.universities.add(university);
        });
    }


    private static void parseSubInfo(University university) throws Exception {
        String base = "http://www.edu.ru";
        URL url = new URL(base+university.url);

        Document doc = Jsoup.parse(url, 100000);
        university.town = doc.select("h1").get(0).childNode(0).toString();

        university.siteUrl = doc.select("a[href][target=_blank][title]").attr("href");
        List<Node> data = null;
      try {
           data = doc.select("td:contains(Телефон):not([class])").get(0).childNodes();
      }
      catch (Exception e) {
          System.out.println("very bad " + university.url);
         throw new Exception("very bad");

      }
        for (int i = 0; i < data.size() ; i++) {
            if (data.get(i).toString().contains("Адрес")) {
                university.fullAddress = data.get(i + 1).toString();
            }
            if (data.get(i).toString().contains("Телефон")) {
                university.phone = data.get(i + 1).toString();
                break;
            }

        }
    }




    public JSONObject toJson(){
        JSONObject result = new JSONObject();
        JSONArray specials = new JSONArray();

        JSONObject ogj;
        for (Special special : this.specials) {
            ogj = new JSONObject();
            ogj.put("form", special.form);
            ogj.put("specialty", special.specialty);
            ogj.put("cualification", special.cualification);
            specials.add(ogj);
        }
        result.put("special", specials);

        result.put("name", name);
        result.put("town", town);
        result.put("site", siteUrl);
        result.put("fullAddress", fullAddress);
        result.put("phone", phone );
        return result;
    }



    private static void parseScecialTraining(University university) throws IOException {
        String base = "http://www.edu.ru";
        StringBuilder getReguest = new StringBuilder();
        boolean isFirst = true;
        for (int i = 0; i < university.url.length(); i++) {
            if (university.url.charAt(i) == 'd' && isFirst) {
                getReguest.append(university.url.charAt(i));
                getReguest.append(university.url.charAt(++i));
                getReguest.append(university.url.charAt(++i));
                ++i;
                getReguest.append(2);
                getReguest.append(0);
                isFirst = false;
                continue;
            }
            getReguest.append(university.url.charAt(i));
        }

        URL url = new URL(base + getReguest.toString());

        Document doc = Jsoup.parse(url, 10000);
        Elements spec = doc.select("table[class=t2]").select("tr");
        spec.remove(0);
        spec.forEach(element -> {
            Special speciality = new Special();
            String[] array = element.text().split(" ");
            speciality.form = array[0];
            int curI = 3;
            boolean flag = true;
            speciality.specialty = "";
            do {
                try {
                    int i = Integer.parseInt(array[curI].replaceAll("\"","").trim().substring(0,1));
                    return;
                } catch (Exception e) {
                    flag = true;
                }
                try{
                    speciality.specialty += array[curI] + " ";
                }catch (Exception e)
                {
                    int d =5;
                }
                curI++;
            }while (flag);

            speciality.cualification = array[9];
            university.specials.add(speciality);
        });
    }

    public static class Special {
        String form;

        String specialty;

        String cualification;


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Special special = (Special) o;

            if (form != null ? !form.equals(special.form) : special.form != null) return false;
            if (specialty != null ? !specialty.equals(special.specialty) : special.specialty != null) return false;
            return !(cualification != null ? !cualification.equals(special.cualification) : special.cualification != null);

        }

        @Override
        public int hashCode() {
            int result = form != null ? form.hashCode() : 0;
            result = 31 * result + (specialty != null ? specialty.hashCode() : 0);
            result = 31 * result + (cualification != null ? cualification.hashCode() : 0);
            return result;
        }
    }

}
