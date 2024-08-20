package sit.int221.integratedapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.int221.integratedapi.dtos.GlobalSettingDTO;
import sit.int221.integratedapi.entities.GlobalSetting;
import sit.int221.integratedapi.exceptions.ItemNotFoundException;
import sit.int221.integratedapi.repositories.primary.GlobalSettingRepository;

import java.util.List;

@Service
@Qualifier("primaryDataSource")
public class GlobalSettingService {

    private final GlobalSettingRepository globalSettingRepository;

//    public GlobalSetting getGlobalSettingById(Integer id) {
//        return globalSettingRepository.findById(id)
//                .orElseThrow(() -> new ItemNotFoundException("Global Setting with id " + id + " does not exist!"));
//    }

    @Autowired
    public GlobalSettingService(GlobalSettingRepository globalSettingRepository){
        this.globalSettingRepository = globalSettingRepository;
    }

    public GlobalSetting getGlobalSettingByName(String name) {
        return globalSettingRepository.findByName(name)
                .orElseThrow(() -> new ItemNotFoundException("Global Setting with name " + name + " does not exist!"));
    }

    public List<GlobalSetting> getAllGlobalSettings() {
        return globalSettingRepository.findAll();
    }

    @Transactional
    public GlobalSetting updateGlobalSetting(String name, GlobalSettingDTO updatedGlobalSettingDTO) {
        if (name.equalsIgnoreCase("Default")) {
            throw new IllegalArgumentException("Updating the 'Default' global setting is not allowed.");
        }

        GlobalSetting globalSetting = globalSettingRepository.findByName(name)
                .orElseThrow(() -> new ItemNotFoundException("Global Setting with name " + name + " does not exist!"));

        if (updatedGlobalSettingDTO.getMaximumTaskLimit() != null) {
            globalSetting.setMaximumTaskLimit(updatedGlobalSettingDTO.getMaximumTaskLimit());
        }

        if (updatedGlobalSettingDTO.isLimitMaximumTask() != globalSetting.isLimitMaximumTask()) {
            globalSetting.setLimitMaximumTask(updatedGlobalSettingDTO.isLimitMaximumTask());
        }

        return globalSettingRepository.save(globalSetting);
    }
}