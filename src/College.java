import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
/**
 * Created by yudzh_000 on 03.01.2016.
 */
public class College {
    public College(String name, String url) {
        this.name = name;
        this.url = url;
    }

    String name;
    String url;
    String town;

    String phone;

    String fullAdress;

    String siteUrl;

    List<Special> specials = new ArrayList<Special>();

    public static void parseFromHtml(Region region) throws IOException {
        String base = "http://www.edu.ru";

        String getRequest = "?sort=&nr=&show_results=&rgn=";
        if(region.urlColleges ==  null) {
            return;
        }
        int curPag = 1;
        String id = "";
        boolean isUseFull = false;
        for (int i = region.urlColleges.indexOf("rgn.") + 4; i < region.urlColleges.length(); i++) {
            if (region.urlColleges.charAt(i) == '/') {
                break;
            } else {
                id += region.urlColleges.charAt(i);
            }
        }
        int prev;
        do {
            prev = region.colleges.size();

            parse(new URL(base + region.urlColleges + getRequest + id + "&spe=&page=" + Integer.toString(curPag)), region);
            curPag++;
        } while (prev != region.colleges.size());

        for (College college : region.colleges) {
            parseSubInfo(college);
            parseScecialTraining(college);
            college.toJSON();
        }


    }


    private static void parse(URL url, Region region) throws IOException {
        LinkedList<String> html = new LinkedList<String>();
        Pattern rus = Pattern.compile(
                "([\\p{IsCyrillic}])+( )*" +
                        "([\\p{IsCyrillic}|\\p{Punct}])*+( )*+" +
                        "[\\p{IsCyrillic}|\\p{Punct}]*" +
                        "( )*[\\p{IsCyrillic}|\\p{Punct}]*" +
                        "( )*[\\p{IsCyrillic}|\\p{Punct}]*" +
                        "( )*[\\p{IsCyrillic}|\\p{Punct}|(&quot;)]*" +
                        "( )*[\\p{IsCyrillic}|\\p{Punct}|(&quot;)]*" +
                        "( )*[\\p{IsCyrillic}|\\p{Punct}|(&quot;)]*" +
                        "( )*[\\p{IsCyrillic}|\\p{Punct}|(&quot;)]*" +
                        "( )*[\\p{IsCyrillic}|\\p{Punct}|(&quot;)]*" +
                        "( )*[\\p{IsCyrillic}|\\p{Punct}|(&quot;)]*");

        Pattern town = Pattern.compile("([;])([\\p{IsCyrillic}]){1}" +
                "([\\p{IsCyrillic}|\\p{Punct}])+" +
                "([\\s])*[\\p{IsCyrillic}|\\p{Punct}]*" +
                "([\\s])*([\\p{IsCyrillic}|\\p{Punct}])*(&nbsp)");

        String urlRegexp = "(/abitur)(.*)(index.php)";

        String line;
        try {


            InputStream is = url.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                html.add(line);
            }
            while (!html.getFirst().equals("<table cellspacing=\"0\" cellpadding=\"3\" width=\"100%\">")) {
                html.removeFirst();
            }
            html.removeFirst();
            Pattern p = Pattern.compile(urlRegexp);
            while (!html.getFirst().equals("</table>")) {
                line = html.getFirst().trim();
                html.removeFirst();
                if (line.isEmpty()) {
                    continue;
                }

                Matcher m = p.matcher(line);
                if (m.find()) {
                    College college = new College();
                    college.url = m.group();
                    Matcher r = rus.matcher(line);
                    Matcher t = town.matcher(line);

                    if(r.find()) {
                        college.name = r.group().substring(0, r.group().length() - 1).replaceAll("(&quot;)", "\"");
                       if(!t.find()) {
                           if(line.indexOf("</td>") == -1) {
                               line += html.get(0);
                               html.remove(0);

                               t = town.matcher(line);
                               t.find();
                               college.town = t.group().substring(1, t.group().length() - 5);
                           }

                       }else {
                           college.town = t.group().substring(1, t.group().length() - 5);

                       }
                        region.colleges.add(college);
                    }
                }
            }
        }catch (FileNotFoundException e) {
            System.out.println(region.name);
        }
    }

    private static void parseSubInfo(College college) throws IOException {
        String base = "http://www.edu.ru";
        URL url = new URL(base+college.url);
        String line;
        InputStream is = url.openStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));


        Pattern urlRegexp = Pattern.compile("([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,6}");
        while ((line = br.readLine()) != null) {
            line = line.replaceAll("((</b>| <a | <b> |<br>))","");



            Matcher urlMatcher = urlRegexp.matcher(line);
            if(line.contains("Адрес:")) {

                line = line.replaceAll("<b>","");
                for (int j = 0; j < line.length(); j++) {
                    if(line.charAt(j) == '<') {
                        line = line.substring(0,j);
                        break;
                    }
                }

                college.fullAdress = line;
            }

            if(line.contains("Телефон:")) {
                line = line.replaceAll("<b>","");
                line = line.replaceAll("Телефон:","").trim();
                college.phone = line;

            }


            if(line.contains("Сайт ")) {
                if(urlMatcher.find()) {
                    college.siteUrl = urlMatcher.group();
                }

            }
        }
    }

    private static void parseScecialTraining(College college) throws IOException {
        String base = "http://www.edu.ru";
        StringBuilder getReguest = new StringBuilder();
        boolean isFirst = true;
        for (int i = 0; i <college.url.length(); i++) {
            if(college.url.charAt(i) == 'd' && isFirst) {
                getReguest.append(college.url.charAt(i));
                getReguest.append(college.url.charAt(++i));
                getReguest.append(college.url.charAt(++i));
                getReguest.append(college.url.charAt(++i));
                getReguest.append(0);
                isFirst = false;
                continue;
            }
            getReguest.append(college.url.charAt(i));
        }

        URL url = new URL(base+getReguest.toString());
        String line;
        InputStream is = url.openStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        ArrayList<String> html = new ArrayList<String>();
        boolean isUsefull  = false;

        while ((line = br.readLine()) != null) {
            if(line.equals("</table>") && isUsefull) {
                break;
            }
            if(isUsefull) {
                html.add(line.trim());
            }

            if(line.equals("<!--<td>0&nbsp;</td><td><b>11</b></td>-->")) {
                isUsefull = true;
            }

        }
        Special cur = null;
        Set<Special> specials = new HashSet<Special>();
        for (int i = 0; i < html.size(); i++) {

            if(html.get(i).contains("Очная"))  {
                cur = new Special();
                cur.form = "Очная";
            } else {
                if (html.get(i).contains("Заочная")) {
                    cur = new Special();
                    cur.form = "Заочная";
                } else {
                    continue;
                }
            }
            line = html.get(i+1);
            for (int j = line.length()-2; j >= 0 ; j--) {
                if(line.charAt(j) == '>') {
                    line = line.substring(j+4,line.length()-5).trim();
                    break;
                }
            }
            cur.specialty = line;
            specials.add(cur);

        }
        college.specials.addAll(specials);


    }


    public  JSONObject toJSON(){
        JSONObject result = new JSONObject();
        JSONArray specials = new JSONArray();

        JSONObject ogj;
        for (Special special : this.specials) {
            ogj = new JSONObject();
            ogj.put("form", special.form);
            ogj.put("specialty", special.specialty);
            specials.add(ogj);
        }
        result.put("special", specials);

        result.put("name", name);
        result.put("town", town);
        result.put("site", siteUrl);
        result.put("fullAddress", fullAdress);
        result.put("phone", phone );
        return result;
    }


    public College() {
    }


    public static class Special {
        String form;

        String specialty;


        @Override
            public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Special special = (Special) o;

            if (form != null ? !form.equals(special.form) : special.form != null) return false;
            return !(specialty != null ? !specialty.equals(special.specialty) : special.specialty != null);

        }

        @Override
        public int hashCode() {
            int result = form != null ? form.hashCode() : 0;
            result = 31 * result + (specialty != null ? specialty.hashCode() : 0);
            return result;
        }



    }
}
