package sit.int221.integratedapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.int221.integratedapi.dtos.GlobalSettingDTO;
import sit.int221.integratedapi.entities.GlobalSetting;
import sit.int221.integratedapi.exceptions.ItemNotFoundException;
import sit.int221.integratedapi.repositories.GlobalSettingRepository;
import java.util.List;

@Service
public class GlobalSettingService {
    @Autowired
    private GlobalSettingRepository globalSettingRepository;

    public List<GlobalSetting> getAllGlobalSettings() {
        return globalSettingRepository.findAll();
    }

    public GlobalSetting getGlobalSettingByName(String name) {
        return globalSettingRepository.findByName(name)
                .orElseThrow(() -> new ItemNotFoundException("Global Setting with name " + name + " does not exist!"));
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