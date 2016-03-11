/**
 * Created by yudzh_000 on 20.01.2016.
 */

import javafx.util.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.print.Doc;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class UcebaParser {
    List<Town> towns = new ArrayList();
    public void initTowns() throws IOException {
        URL url = new URL("http://www.ucheba.ru/");

        Document doc = Jsoup.parse(url,10000);

        Elements towns = doc.select("ul.location-block-city-list");
        towns = towns.select("a[href]");
        for (Element el: towns
             ) {
            Town town = new Town();
            town.url = el.attr("href").substring(2).replace("?domainConfirm","");
            town.name = el.childNode(2).toString().trim();
            this.towns.add(town);
        }
        initUniversitites();
        for (Town town : this.towns) {
            for (UniversityUcheba university : town.universities) {
                findImages(university);
            }
        }
    }


    public static void findImages(UniversityUcheba university) {
        boolean flag = false;
        do {
            try {
                university.imageUrl = ImageSearcher.searcher.getImageUrl(university.name);
                university.logoUrl = ImageSearcher.searcher.getLogoUrl(university.name);
                flag = false;
            } catch (Exception e) {
                flag = true;
            }
        }while (flag);
    }


    static int numb = 1;

    private void initUniversitites() throws IOException {
       towns.parallelStream().forEach(town ->  {
           URL url = null;
           try {
               url = new URL("http://" + town.url + "for-abiturients/vuz?s=0");
           } catch (MalformedURLException e) {
               e.printStackTrace();
           }
           Document doc = getDocument(url);

           Elements universities = doc.select("section.search-results-item");
           universities.stream().forEach( element ->
                   town.universities.add(UniversityUcheba.createUniver(element,town.name))
           );

           System.out.println(town.name + "  " + Integer.toString(numb)+ "/" + Integer.toString(towns.size()));
           numb++;
        });
    }

    public static Document getDocument(URL url) {
        Document result;
        try {
            result = Jsoup.parse(url,10000);
        } catch (IOException e) {
            return getDocument(url);
        }
        return result;
    }

    public boolean isContains(String town_name) {
        for (Town town : towns) {
            if (town.name.equals(town_name)) {
                return true;
            }
        }
        return false;
    }

    public boolean isContains(String townName, String university, String logoUrl) {
        if(isContains(townName)) {
            Town curTown = null;
            for (Town town : towns) {
                if(town.name.equals(townName)) {
                    curTown = town;
                    break;
                }
            }
            for (UniversityUcheba universityUcheba : curTown.universities) {
                if(universityUcheba.name.equals(university) || universityUcheba.logoUrl.equals(logoUrl) ) {
                    return true;
                }
             }
        }
        return false;
    }

    public UniversityUcheba getUniversity(String townName, String university, String logoUrl) {
        if(isContains(townName)) {
            Town curTown = null;
            for (Town town : towns) {
                if (town.name.equals(townName)) {
                    curTown = town;
                    break;
                }
            }
            for (UniversityUcheba universityUcheba : curTown.universities) {
                if (universityUcheba.name.equals(university) || universityUcheba.logoUrl.equals(logoUrl)) {
                    return universityUcheba;
                }
            }
        }
        return null;
    }

    public Town getTown(String townName) {
        if (isContains(townName)) {
            for (Town town : towns) {
                if (town.name.equals(townName)) {
                    return town;
                }

            }
        }
        return null;
    }

}

class Town{
    String name;
    String url;
    List<UniversityUcheba>  universities = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        String town = (String) o;

        return name != null ? name.toLowerCase().equals(town.toLowerCase()) : town == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

}

class UniversityUcheba{
    public boolean isParsed = false;
    String logoUrl;
    String imageUrl;
    String name;
    String url;
    String description;
    String town;
    List<Speciality> specialityList = new ArrayList<>();

    public static UniversityUcheba createUniver(Element html,String town){
        UniversityUcheba univ = new UniversityUcheba();
        univ.town = town.trim();
        Element link = html.select("a[href]").select("[class^=js_webstat]").get(0);
        univ.url = link.attr("href");
        univ.name = link.childNodes().get(0).toString();
        try {
            getDescription(univ);
            initSpecialities(univ);
        }catch (IOException e) {
            e.printStackTrace();
        }
        boolean flag = false;
        return univ;
    }



    private static void getDescription(UniversityUcheba university) throws IOException {
        Document document;
        try {
            document = Jsoup.parse(new URL("http://ucheba.ru/" + university.url),10000);
        } catch (Exception e) {
            getDescription(university);
            return;
        }
        Elements elements = document.select("div.branding-discription-lead");
        String description = "";
        if (elements.size() == 0) {
            elements = document.select("div.head-announce__lead");
        }
        if(elements.size() != 0) {
            for (Element el: elements
                 ) {
                description += el.childNode(0).toString();
            }
        }

        university.description = description.replaceAll("&nbsp;","").replaceAll("\\n","");
    }

    private static void initSpecialities(UniversityUcheba university) throws IOException {
        int cur = 0;
        Document document;
        try {
            document = Jsoup.parse(new URL("http://ucheba.ru/" + university.url+"/programs?s="+ cur),10000);
        } catch (Exception e) {
            initSpecialities(university);
            return;
        }
        Elements elements = document.select("h3.search-results-title");
        elements.stream().forEach(element -> {
            try {
                Element el = element.select("a[href]").select("[class^=js_webstat]").get(0);
                university.specialityList.addAll(Speciality.initSpeciality(el.childNode(0).toString(),
                        new URL("http://www.ucheba.ru"+el.attr("href"))));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public String toString() {
        return this.name;
    }

    private static Pair<String, String> getImageURL(Element html){
        Elements pictures = html.select("img[src$=.png]");
        if (pictures.size() <= 0) {
            pictures = html.select("img[src$=.jpg]");
            if(pictures.size() == 0){
                return null;
            }
            String res = pictures.get(0).attr("src");
            return new Pair<>(res,"jpg");
        } else {

            return new Pair<>(pictures.get(0).attr("src"),"png");
        }
    }

    public JSONObject toJson() {
        JSONObject res = new JSONObject();
        JSONArray speciality = new JSONArray();
        for (Speciality spec:this.specialityList) {
            JSONObject obj = new JSONObject();
            obj.put("name", spec.name);
            obj.put("form", spec.form);
            obj.put("description", spec.description);
            obj.put("price", spec.price);
            obj.put("placeCount", spec.placeCount);
            obj.put("points", spec.points);
            obj.put("subjects", spec.subjects);
            obj.put("duration", spec.duration);
            speciality.add(obj);
        }

        res.put("town",this.town);
        res.put("name",this.name);
        res.put("description",this.description);
        res.put("specialities", speciality);
        res.put("logoUrl",this.logoUrl);

        return  res;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null ) return false;

        String that = (String) o;

        return name.equals(that);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public boolean isCorrect() {
        return  !(name.isEmpty() || url.isEmpty());
    }
}

class Speciality {
    String name;
    String form;
    String description;
    int price;
    int placeCount;
    int points;
    String subjects;
    String duration;

    public static List<Speciality> initSpeciality(String name, URL url) throws IOException {
        Speciality speciality = new Speciality();
        speciality.name = name;
        ArrayList<Speciality> result = new ArrayList<>();
        Document document = null;
        boolean flag = false;
        document = UcebaParser.getDocument(url);

        Elements descriptionPart = document.select("div.head-announce__lead");
        if(descriptionPart.size()>0) {
            speciality.description = descriptionPart.get(0).childNode(0).toString()
                    .replaceAll("&nbsp;", " ").replaceAll("\n","");
        }



        Elements parameters = document.select("td.ttf-col-2").select("div");
        if(parameters.size()>0) {
            sub(parameters,speciality);
            result.add(speciality);
        }
        parameters = document.select("td.ttf-col-3").select("div");
        if(parameters.size()>0) {
            Speciality spec = new Speciality();
            spec.name = name;
            sub(parameters,spec);
            result.add(spec);
        }


        return result;

    }

    private static void sub(Elements el, Speciality speciality) {
        for (int i = 0; i < el.size() - 1; i++){
            if(el.get(i).childNode(0).toString().contains("Форма обучения")) {
                speciality.form = el.get(i+1).childNode(0).toString().replaceAll("\n","");
                continue;
            }
            if(el.get(i).childNode(0).toString().contains("Стоимость")) {
                try {
                    speciality.price = Integer.parseInt(el.get(i + 1).childNode(0).toString().
                            replaceAll("&nbsp;", "").replaceAll("\n", "").trim());
                }
                catch (Exception e) {
                    continue;
                }
                continue;
            }
            if(el.get(i).childNode(0).toString().contains("Проходной балл")) {
                try {
                    speciality.points = Integer.parseInt(el.get(i+1).childNode(0).toString().replaceAll("\n",""));
                }
                catch (Exception e) {
                    continue;
                }
                continue;
            }
            if(el.get(i).childNode(0).toString().contains("Бюджетных мест")) {
                try {
                    speciality.placeCount = Integer.parseInt(el.get(i+1).childNode(0).toString().replaceAll("\n",""));
                }
                catch (Exception e) {
                    continue;
                }
                continue;
            }
            if(el.get(i).childNode(0).toString().contains("Срок обучения")) {
                speciality.duration = el.get(i+1).childNode(0).toString().replaceAll("\n","");
                continue;
            }
            if(el.get(i).childNode(0).toString().contains("Предметы ЕГЭ")) {
                speciality.subjects = el.get(i+1).childNode(0).toString().replaceAll("\n","");
                continue;
            }
        }

    }
}