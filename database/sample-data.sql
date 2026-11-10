-- Optional MySQL-only sample inventory. The local H2 profile seeds the full
-- 20-property set automatically. These rows use distinct local image files.
USE estatepro;

INSERT INTO properties
(id,title,description,property_type,listing_type,city,locality,address,price,bedrooms,bathrooms,area_sqft,image_url,status,owner_id,agent_id,featured,created_at)
VALUES
(UUID(),'The Aurelia Residence','A considered home with generous natural light and thoughtful spaces.','Villa','For Sale','Chennai','Adyar','Adyar, Chennai',48500000,4,2,3200,'/assets/images/properties/villa-01.jpg','PUBLISHED',NULL,NULL,TRUE,NOW(6)),
(UUID(),'Skyline Penthouse','A refined city residence with bright, generous interiors.','Apartment','For Sale','Chennai','OMR','OMR, Chennai',22800000,3,2,2100,'/assets/images/properties/apartment-06.jpg','PUBLISHED',NULL,NULL,TRUE,NOW(6)),
(UUID(),'The Banyan House','An independent home in a welcoming neighbourhood.','Independent house','For Sale','Coimbatore','Race Course','Race Course, Coimbatore',31500000,4,2,2800,'/assets/images/properties/villa-02.jpg','PUBLISHED',NULL,NULL,TRUE,NOW(6)),
(UUID(),'Lakeside Veranda','A light-filled villa with space for a slower pace.','Villa','For Sale','Bengaluru','Whitefield','Whitefield, Bengaluru',42000000,4,2,3400,'/assets/images/properties/villa-03.jpg','PUBLISHED',NULL,NULL,TRUE,NOW(6)),
(UUID(),'Altura Heights','Contemporary apartment living close to the city.','Apartment','For Rent','Hyderabad','Jubilee Hills','Jubilee Hills, Hyderabad',85000,3,2,1850,'/assets/images/properties/apartment-07.jpg','PUBLISHED',NULL,NULL,TRUE,NOW(6)),
(UUID(),'The Palm Court','A beautifully proportioned city home near the coast.','Apartment','For Rent','Mumbai','Bandra West','Bandra West, Mumbai',175000,3,2,1750,'/assets/images/properties/apartment-08.jpg','PUBLISHED',NULL,NULL,TRUE,NOW(6)),
(UUID(),'Serene Grove','A private family villa with generous indoor-outdoor space.','Villa','For Sale','Chennai','ECR','ECR, Chennai',72500000,5,2,4100,'/assets/images/properties/villa-04.jpg','PUBLISHED',NULL,NULL,TRUE,NOW(6)),
(UUID(),'Indigo House','A calm independent home close to local amenities.','Independent house','For Sale','Coimbatore','Saibaba Colony','Saibaba Colony, Coimbatore',18700000,3,2,2200,'/assets/images/properties/interior-08.jpg','PUBLISHED',NULL,NULL,TRUE,NOW(6)),
(UUID(),'Cedar & Stone','A considered apartment with a welcoming, modern interior.','Apartment','For Rent','Bengaluru','Indiranagar','Indiranagar, Bengaluru',65000,2,2,1420,'/assets/images/properties/interior-09.jpg','PUBLISHED',NULL,NULL,TRUE,NOW(6)),
(UUID(),'The Meridian Suite','Flexible commercial space in a connected district.','Commercial office','For Sale','Hyderabad','Gachibowli','Gachibowli, Hyderabad',17600000,0,1,1980,'/assets/images/properties/commercial-02.jpg','PUBLISHED',NULL,NULL,TRUE,NOW(6)),
(UUID(),'Harbourview Residence','A bright, well-connected home with thoughtful finishes.','Apartment','For Rent','Mumbai','Powai','Powai, Mumbai',125000,2,2,1320,'/assets/images/properties/apartment-09.jpg','PUBLISHED',NULL,NULL,TRUE,NOW(6)),
(UUID(),'Solstice Villa','A private coastal retreat designed for family living.','Villa','For Sale','Chennai','Kovalam','Kovalam, Chennai',56000000,4,2,3600,'/assets/images/properties/villa-05.jpg','PUBLISHED',NULL,NULL,TRUE,NOW(6)),
(UUID(),'The Atelier Offices','A considered commercial address with room to grow.','Commercial office','For Lease','Bengaluru','Koramangala','Koramangala, Bengaluru',225000,0,1,2400,'/assets/images/properties/commercial-01.jpg','PUBLISHED',NULL,NULL,TRUE,NOW(6)),
(UUID(),'Gardenia House','A peaceful independent home with generous natural light.','Independent house','For Sale','Coimbatore','Vadavalli','Vadavalli, Coimbatore',14900000,3,2,2050,'/assets/images/properties/villa-06.jpg','PUBLISHED',NULL,NULL,TRUE,NOW(6)),
(UUID(),'Parkside Residences','A welcoming apartment near parks and daily essentials.','Apartment','For Rent','Hyderabad','Kondapur','Kondapur, Hyderabad',42000,2,2,1280,'/assets/images/properties/apartment-10.jpg','PUBLISHED',NULL,NULL,TRUE,NOW(6)),
(UUID(),'The Foundry','A spacious commercial address for ambitious teams.','Commercial office','For Sale','Mumbai','Andheri East','Andheri East, Mumbai',44500000,0,1,3100,'/assets/images/properties/commercial-04.jpg','PUBLISHED',NULL,NULL,TRUE,NOW(6)),
(UUID(),'Maison Willow','A thoughtfully detailed home in an established neighbourhood.','Apartment','For Sale','Chennai','Nungambakkam','Nungambakkam, Chennai',24500000,3,2,1900,'/assets/images/properties/interior-10.jpg','PUBLISHED',NULL,NULL,TRUE,NOW(6)),
(UUID(),'Bluebell Courtyard','A contemporary family villa with a quiet garden setting.','Villa','For Sale','Bengaluru','Sarjapur Road','Sarjapur Road, Bengaluru',27900000,4,2,2950,'/assets/images/properties/villa-07.jpg','PUBLISHED',NULL,NULL,TRUE,NOW(6)),
(UUID(),'The Terrace House','An inviting apartment with an easy neighbourhood rhythm.','Apartment','For Sale','Coimbatore','RS Puram','RS Puram, Coimbatore',9200000,2,2,1180,'/assets/images/properties/interior-11.jpg','PUBLISHED',NULL,NULL,TRUE,NOW(6)),
(UUID(),'Eastline Studios','Adaptable commercial space in a growing business district.','Commercial office','For Sale','Hyderabad','Madhapur','Madhapur, Hyderabad',17800000,0,1,1650,'/assets/images/properties/commercial-05.jpg','PENDING',NULL,NULL,TRUE,NOW(6));
