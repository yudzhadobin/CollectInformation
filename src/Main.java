import com.sun.org.apache.xpath.internal.SourceTree;
import com.sun.prism.Image;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by yudzh_000 on 03.01.2016.
 */
public class Main {
    public static void main(String[] args) {



        List<Region> regions= getAllRegions();

        for (Region region : regions) {
            System.out.println(region.name+ " " + region.universities.size());
        }

        UcebaParser ucebaParser = new UcebaParser();
        try {
            ucebaParser.initTowns();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<RegionFinal> result = merge(regions,ucebaParser);

    }

    public static void saveToFile(RegionFinal region) {
        File file = new File("D:\\MyPrj\\UniverDownloader\\"+region.name+".json");
        file.getParentFile().mkdirs();
        try {
            FileWriter writer = new FileWriter(file);

        writer.write(region.toJson().toJSONString());
        writer.flush();
        }catch (Exception e) {
            System.out.println("cant save");
        }
    }

    public static List<RegionFinal> merge(List<Region> regions, UcebaParser universFromUcheba) {
        List<RegionFinal> regionsFinal = new ArrayList<>();
        int i = 1;
        for (Region region : regions) {
            RegionFinal regionFinal = new RegionFinal();
            regionFinal.name = region.name;
            Set<Town> parsedTown = new HashSet<Town>();
            region.universities.forEach(university -> {
                if (!regionFinal.isContains(university.town)) {
                    regionFinal.towns.add(new TownFinal(university.town));
                }
                TownFinal curTown = regionFinal.getTown(university.town);

                if(universFromUcheba.isContains(university.town)) {
                    parsedTown.add(universFromUcheba.getTown(university.town));
                }

                boolean isExistsInUceba = universFromUcheba.isContains(university.town, university.name,university.logoUrl);
                if (isExistsInUceba) {
                    UniversityUcheba univ = universFromUcheba.getUniversity(university.town,university.name, university.logoUrl);
                    univ.isParsed = true;
                    UniversityFinal universityFinal = new UniversityFinal(univ,university);
                    universityFinal.region = region.name;
                    curTown.universities.add(universityFinal);
                } else {
                    UniversityFinal universityFinal = new UniversityFinal(university);
                    universityFinal.region = region.name;
                    curTown.universities.add(universityFinal);
                }
            });
            for (Town town : parsedTown) {
                TownFinal curTown = regionFinal.getTown(town.name);
                for (UniversityUcheba university : town.universities) {
                    if(!university.isParsed) {
                        university.isParsed = true;
                        UniversityFinal universityFinal = new UniversityFinal(university);
                        universityFinal.region = region.name;
                        curTown.universities.add(universityFinal);
                    }
                }
            }
            regionsFinal.add(regionFinal);
            System.out.println(regionFinal.name + "  " + i + "/83");
            i++;
            saveToFile(regionFinal);
        }
        return regionsFinal;
    }


    public static List<Region> getAllRegions() {
        List<Region> regions = null;
        try {
            regions = Region.parseFromHtml();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return regions;
    }


}

class RegionFinal {
    String name;
    List<TownFinal> towns = new ArrayList<>();
    List<UniversityFinal> errors = new ArrayList<>();

    public boolean isContains(String name) {
        for (TownFinal town : towns) {
            if(town.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public TownFinal getTown(String name) {
        for (int i = 0; i< towns.size(); i++) {
            if(towns.get(i).name.equals(name)) {
                return towns.get(i);
            }
        }
        return null;
    }

    public JSONObject toJson() {
        JSONObject result = new JSONObject();

        result.put("region_name",this.name);

        JSONArray towns = new JSONArray();

        this.towns.forEach(townFinal -> {
            towns.add(townFinal.toJson());
        });

        result.put("towns", towns);
        return result;
    }
}

class TownFinal {
    List<UniversityFinal> universities = new ArrayList<>();
    String name;

    public TownFinal(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        String town = (String) o;

        return name != null ? name.trim().toLowerCase().equals(town.trim().toLowerCase()) : town == null;

    }

    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        result.put("town_name",this.name);

        JSONArray universities = new JSONArray();

        this.universities.forEach(university-> {
            universities.add(university.toJson());
        });

        result.put("universities", universities);
        return result;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}


class UniversityFinal {
    String name;
    String phone;
    String address;
    String siteUrl;
    String town;
    String region;
    String logoUrl;
    String imageUrl;
    String description;

    boolean hasImages = false;

    List<Speciality> specialities = new ArrayList<>();

    public UniversityFinal(UniversityUcheba university) {
        this.name = university.name;
        this.description = university.description;
        this.town = university.town;

        university.specialityList.forEach(speciality -> {
            Speciality spec = new Speciality();
            spec.description = speciality.description;
            spec.duration = speciality.duration;
            spec.form = speciality.form;
            spec.name = speciality.name;
            spec.placeCount = speciality.placeCount;
            spec.points = speciality.points;
            spec.price = speciality.price;
            spec.subjects = speciality.subjects;
            this.specialities.add(spec);
        });



    }

    public UniversityFinal(University university) {
        this.name = university.name;
        this.siteUrl = university.siteUrl;
        this.address = university.fullAddress;
        this.town = university.town;
        this.phone = university.phone;
        university.specials.forEach(speciality -> {
            Speciality spec = new Speciality();
            spec.qualification = speciality.cualification;
            spec.form = speciality.form;
            spec.name = speciality.specialty;

            this.specialities.add(spec);
        });


    }

    public UniversityFinal(UniversityUcheba universityUcheba,University university) {
        this(universityUcheba);
        this.phone = university.phone;
        this.address = university.fullAddress;
        this.siteUrl = university.siteUrl;
        this.imageUrl = university.imageUrl;
        this.logoUrl = university.logoUrl;
    }


    public void findImages() {
        boolean flag = false;
        do {
            try {
                this.imageUrl = ImageSearcher.searcher.getImageUrl(name);
                this.logoUrl = ImageSearcher.searcher.getLogoUrl(name);
                flag = false;
            } catch (Exception e) {
                flag = true;
            }
        }while (flag);
    }

    public JSONObject toJson(){
        JSONObject result = new JSONObject();

        result.put("university_name", this.name);
        result.put("phone", phone);
        result.put("address", address);
        result.put("site_url", siteUrl);
        result.put("town_name", town);
        result.put("region", region);
        result.put("description", description);
        result.put("logo_url", logoUrl);
        result.put("image_url", imageUrl);

        JSONArray jsonArray = new JSONArray();

        this.specialities.forEach(speciality -> {
            jsonArray.add(speciality.toJson());
        });

        result.put("specialities", jsonArray);

        return result;
    }

    private class Speciality {
        String name;
        String form;
        String description;
        int price;
        int placeCount;
        int points;
        String subjects;
        String duration;
        String qualification;

        JSONObject toJson() {
            JSONObject result = new JSONObject();

            result.put("speciality_name", name);
            result.put("form", form);
            result.put("description", description);
            result.put("subjects", subjects);
            result.put("duration", duration);
            result.put("qualification", qualification);

            result.put("price", price);
            result.put("places", placeCount);
            result.put("points",points);
            return result;
        }
    }
}