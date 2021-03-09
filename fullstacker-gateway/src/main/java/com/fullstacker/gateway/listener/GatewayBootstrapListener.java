package me.fullstacker.gateway.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import me.fullstacker.util.admin.repository.UserPreferenceRepository;
import me.fullstacker.util.constants.Constants;
import me.fullstacker.util.dto.BuildVersionDTO;
import me.fullstacker.util.service.BuildVersionService;
import me.fullstacker.util.service.PreferenceUtilService;
import me.fullstacker.util.service.TranslationsService;

@Component
public class GatewayBootstrapListener implements ApplicationListener<ApplicationReadyEvent> {

	private final Logger log = LoggerFactory.getLogger(GatewayBootstrapListener.class);

	@Autowired
	private PreferenceUtilService prefService;
	@Autowired
	private TranslationsService translationService;
	@Autowired
	private UserPreferenceRepository upRepo;
	@Autowired
	private BuildVersionService buildVersionService;
	
	@Value("${spring.application.name}")
	private String appName;

	@Value("${spring.profiles.active}")
	private String profileName;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		log.info("Caching Application Level data");

		prefService.cacheAppPreferences();
		prefService.cacheUserPreferences();
		
		//Cache translations for all the locales chosen by users
		upRepo.fetchUserLocales().forEach(locale -> translationService.cacheTranslations(locale));
		
		if(!Constants.SPRING_PROFILE_DEVELOPMENT.equals(profileName)) {
			try {
				YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
				PropertySource<?> propertySource = loader.load("config/build.yml", new ClassPathResource("config/build.yml")).get(0);
				String buildVersionNo = (String) propertySource.getProperty("build.revision");
				Long buildTime = (Long) propertySource.getProperty("build.timestamp");
				buildVersionService.saveBuildVersion(new BuildVersionDTO(appName, buildVersionNo, buildTime));
			} catch (Exception e) {
				log.error("Exception while storing build version", e);
			}
		}
	}
	
}
