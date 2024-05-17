package sit.int221.intregratedapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sit.int221.intregratedapi.dtos.GlobalSettingDTO;
import sit.int221.intregratedapi.entities.GlobalSetting;
import sit.int221.intregratedapi.services.GlobalSettingService;
import sit.int221.intregratedapi.utils.ListMapper;

import java.util.List;

@RestController
@RequestMapping("/v2/global-settings")
public class GlobalSettingController {
    @Autowired
    private GlobalSettingService globalSettingService;
    @Autowired
    private ListMapper listMapper;

    @GetMapping
    public ResponseEntity<List<GlobalSettingDTO>> getAllGlobalSettings() {
        List<GlobalSetting> globalSettings = globalSettingService.getAllGlobalSettings();
        List<GlobalSettingDTO> globalSettingDTOs = listMapper.mapList(globalSettings, GlobalSettingDTO.class);
        return ResponseEntity.ok(globalSettingDTOs);
    }
}