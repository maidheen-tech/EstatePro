package com.estatepro.config;

import com.estatepro.entity.*;
import com.estatepro.repository.AppointmentRepository;
import com.estatepro.repository.BookingRepository;
import com.estatepro.repository.EnquiryRepository;
import com.estatepro.repository.FavoriteRepository;
import com.estatepro.repository.PropertyRepository;
import com.estatepro.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
@Profile("local")
public class LocalSampleData implements CommandLineRunner {
    private final PropertyRepository properties;
    private final UserRepository users;
    private final FavoriteRepository favorites;
    private final EnquiryRepository enquiries;
    private final AppointmentRepository appointments;
    private final BookingRepository bookings;
    private final PasswordEncoder passwords;
    private final String demoPassword;
    public LocalSampleData(PropertyRepository properties, UserRepository users, FavoriteRepository favorites,
                           EnquiryRepository enquiries, AppointmentRepository appointments, BookingRepository bookings,
                           PasswordEncoder passwords,
                           @Value("${estatepro.demo.password:}") String demoPassword) {
        this.properties = properties; this.users = users; this.favorites = favorites; this.enquiries = enquiries;
        this.appointments = appointments; this.bookings = bookings; this.passwords = passwords; this.demoPassword = demoPassword;
    }
    @Override
    public void run(String... args) {
        createDemoUsers();
        if (properties.count() == 0) seedProperties();
        seedDemoWorkflows();
    }
    private void seedProperties() {
        User seller = users.findByEmail("seller@estatepro.local").orElse(null);
        User agent = users.findByEmail("agent@estatepro.local").orElse(null);
        String[][] homes = {
                {"The Aurelia Residence","Chennai","Adyar","Villa","4","3200","48500000","villa-01.jpg"},
                {"Skyline Penthouse","Chennai","OMR","Apartment","3","2100","22800000","apartment-06.jpg"},
                {"The Banyan House","Coimbatore","Race Course","Independent house","4","2800","31500000","villa-02.jpg"},
                {"Lakeside Veranda","Bengaluru","Whitefield","Villa","4","3400","42000000","villa-03.jpg"},
                {"Altura Heights","Hyderabad","Jubilee Hills","Apartment","3","1850","85000","apartment-07.jpg","For Rent"},
                {"The Palm Court","Mumbai","Bandra West","Apartment","3","1750","175000","apartment-08.jpg","For Rent"},
                {"Serene Grove","Chennai","ECR","Villa","5","4100","72500000","villa-04.jpg"},
                {"Indigo House","Coimbatore","Saibaba Colony","Independent house","3","2200","18700000","interior-08.jpg"},
                {"Cedar & Stone","Bengaluru","Indiranagar","Apartment","2","1420","65000","interior-09.jpg","For Rent"},
                {"The Meridian Suite","Hyderabad","Gachibowli","Apartment","3","1980","17600000","commercial-02.jpg"},
                {"Harbourview Residence","Mumbai","Powai","Apartment","2","1320","125000","apartment-09.jpg","For Rent"},
                {"Solstice Villa","Chennai","Kovalam","Villa","4","3600","56000000","villa-05.jpg"},
                {"The Atelier Offices","Bengaluru","Koramangala","Commercial office","0","2400","225000","commercial-01.jpg","For Lease"},
                {"Gardenia House","Coimbatore","Vadavalli","Independent house","3","2050","14900000","villa-06.jpg"},
                {"Parkside Residences","Hyderabad","Kondapur","Apartment","2","1280","42000","apartment-10.jpg","For Rent"},
                {"The Foundry","Mumbai","Andheri East","Commercial office","0","3100","44500000","commercial-04.jpg"},
                {"Maison Willow","Chennai","Nungambakkam","Apartment","3","1900","24500000","interior-10.jpg"},
                {"Bluebell Courtyard","Bengaluru","Sarjapur Road","Villa","4","2950","27900000","villa-07.jpg"},
                {"The Terrace House","Coimbatore","RS Puram","Apartment","2","1180","9200000","interior-11.jpg"},
                {"Eastline Studios","Hyderabad","Madhapur","Commercial office","0","1650","17800000","commercial-05.jpg"}
        };
        for (int i = 0; i < homes.length; i++) {
            String[] home = homes[i];
            Property property = new Property(home[0], "A considered home with generous natural light, refined finishes and thoughtfully planned spaces. Set in a well-connected neighbourhood close to everyday essentials.", home[3], home.length > 8 ? home[8] : "For Sale", home[1], home[2], home[2] + ", " + home[1],
                    new BigDecimal(home[6]), Integer.valueOf(home[4]), home[4].equals("0") ? 1 : 2,
                    Integer.valueOf(home[5]), "/assets/images/properties/" + home[7],
                    i == homes.length - 1 ? ListingStatus.PENDING : ListingStatus.PUBLISHED,
                    seller != null && i % 4 == 0 ? seller : null, true);
            if (agent != null && i % 3 == 0) property.setAgent(agent);
            properties.save(property);
        }
    }
    private void seedDemoWorkflows() {
        if (demoPassword.isBlank() || enquiries.count() > 0) return;
        User buyer = users.findByEmail("buyer@estatepro.local").orElse(null);
        User agent = users.findByEmail("agent@estatepro.local").orElse(null);
        Property property = properties.findByStatusOrderByCreatedAtDesc(ListingStatus.PUBLISHED).stream().findFirst().orElse(null);
        if (buyer == null || agent == null || property == null) return;
        if (favorites.findByUserIdOrderByCreatedAtDesc(buyer.getId()).isEmpty())
            favorites.save(new Favorite(buyer, property));
        enquiries.save(new Enquiry(property, buyer, "I would love to arrange a viewing and learn more about the neighbourhood."));
        if (appointments.findByAgentIdOrderByVisitDateAscVisitTimeAsc(agent.getId()).isEmpty())
            appointments.save(new Appointment(property, buyer, LocalDate.now().plusDays(2), LocalTime.of(10, 0)));
        Booking booking = new Booking("EP-DEMO-0001", property, buyer);
        booking.setStatus("CONFIRMED");
        bookings.save(booking);
    }
    private void createDemoUsers() {
        if (demoPassword.isBlank()) return;
        for (Role role : Role.values()) {
            String email = role.name().toLowerCase() + "@estatepro.local";
            if (!users.existsByEmail(email)) users.save(new User(email, passwords.encode(demoPassword),
                    role == Role.ADMIN ? "EstatePro Administrator" : "EstatePro " + role.name().toLowerCase(),
                    role));
        }
    }
}
