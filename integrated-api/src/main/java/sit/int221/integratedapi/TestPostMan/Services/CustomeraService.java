package sit.int221.integratedapi.TestPostMan.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sit.int221.integratedapi.TestPostMan.Entities.Customera;
import sit.int221.integratedapi.TestPostMan.Repositories.CustomeraRepository;

import java.util.List;

@Service
public class CustomeraService {
    @Autowired
    CustomeraRepository customeraRepository;
    public List<Customera> insertCustomeras(List<Customera> customeras) {
        return customeraRepository.saveAll(customeras);
    }
    public List<Customera> findAllCustomera() {
        return findAllCustomera(null);
    }
    public List<Customera> findAllCustomera(String name) {
        if (name==null || name.isEmpty()) {
            return customeraRepository.findAll();
        } else {
            return customeraRepository.findByFirstNameContains(name);
        }
    }
}
