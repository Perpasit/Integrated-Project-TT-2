package sit.int221.intregratedapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.intregratedapi.dtos.GlobalSettingDTO;
import sit.int221.intregratedapi.entities.GlobalSetting;
import sit.int221.intregratedapi.services.GlobalSettingService;
import sit.int221.intregratedapi.utils.ListMapper;

@RestController
@RequestMapping("/v2/global-settings")
public class GlobalSettingController {
    @Autowired
    private GlobalSettingService globalSettingService;

    @Autowired
    private ListMapper listMapper;

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