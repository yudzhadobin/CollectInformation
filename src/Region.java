import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yudzh_000 on 03.01.2016.
 */
public class Region implements Comparable<Region> {
    String name;
    String urlColleges;
    String urlUniversities;

    String url;
    List<University> universities = new ArrayList<>();
    List<College> colleges = new ArrayList<College>();
    JSONObject jsonObject;
    public Region() {
    }

    static List<Region> parseFromHtml() throws IOException {
        List<Region> result = new ArrayList<Region>();
        parse(result);
        result.parallelStream().forEach(region -> {
            try {
                University.parseFromHtml(region);
            } catch (IOException e) {
                e.printStackTrace();

            }
        });
        for (Region region : result) {
            for (University university : region.universities) {
                findImages(university);
            }
        }
        return result;
    }

    public static void findImages(University university) {
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

    private static void parse(List<Region> list) throws IOException {
        String url = "http://www.edu.ru/abitur/act.4/index.php";


        URL baseUrl = new URL(url);
        Document document = Jsoup.parse(baseUrl,10000);
        Elements regions = document.select("table[style=margin:10px 0px;][width=100%]").select("a[href]");
        for (Element region: regions) {
            Region reg = new Region();
            reg.name = region.childNode(0).toString();
            reg.url = region.attr("href");
            list.add(reg);
        }


    }

    public JSONObject toJSON() {
        JSONObject result = new JSONObject();
        result.put("name", this.name);
        JSONArray colleges = new JSONArray();
        for (College college : this.colleges) {
            colleges.add(college.toJSON());
        }
        JSONArray universities = new JSONArray();
        for (ConvertableToJson university: this.universities) {
            universities.add(university.toJson());
        }
        result.put("universities", universities);
        result.put("colleges",colleges);

        this.jsonObject = result;
        return  result;
    }

    public void removeUniv(University univ) {
        universities.remove(univ);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Region region = (Region) o;

        return !(name != null ? !name.equals(region.name) : region.name != null);

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public int compareTo(Region o) {
        return this.name.compareTo(o.name);
    }
}
