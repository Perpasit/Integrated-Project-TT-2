package sit.int221.integratedapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.integratedapi.dtos.GlobalSettingDTO;
import sit.int221.integratedapi.entities.GlobalSetting;
import sit.int221.integratedapi.services.GlobalSettingService;
import sit.int221.integratedapi.utils.ListMapper;
import java.util.List;

@RestController
@RequestMapping("/v2/global-settings")
@CrossOrigin(origins = {"http://localhost:4173/", "http://localhost:5173/", "http://ip23tt2.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th"})
public class GlobalSettingController {
    @Autowired
    private GlobalSettingService globalSettingService;

    @Autowired
    private ListMapper listMapper;

    @GetMapping("")
    public ResponseEntity<List<GlobalSettingDTO>> getAllGlobalSettings() {
        List<GlobalSetting> globalSettings = globalSettingService.getAllGlobalSettings();
        List<GlobalSettingDTO> globalSettingDTOs = listMapper.mapList(globalSettings, GlobalSettingDTO.class);
        return ResponseEntity.ok(globalSettingDTOs);
    }

    @GetMapping("/{name}")
    public ResponseEntity<GlobalSettingDTO> getGlobalSettingByName(@PathVariable String name) {
        GlobalSetting globalSetting = globalSettingService.getGlobalSettingByName(name);
        GlobalSettingDTO globalSettingDTO = listMapper.mapObject(globalSetting, GlobalSettingDTO.class);
        return ResponseEntity.ok(globalSettingDTO);
    }

    @PatchMapping("/{name}")
    public ResponseEntity<GlobalSettingDTO> updateGlobalSetting(
            @PathVariable String name,
            @RequestBody GlobalSettingDTO updatedGlobalSettingDTO
    ) {
        GlobalSetting updatedGlobalSetting = globalSettingService.updateGlobalSetting(name, updatedGlobalSettingDTO);
        GlobalSettingDTO globalSettingDTO = listMapper.mapObject(updatedGlobalSetting, GlobalSettingDTO.class);
        return ResponseEntity.ok(globalSettingDTO);
    }
}