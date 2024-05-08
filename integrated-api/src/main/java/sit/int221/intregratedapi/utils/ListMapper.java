package sit.int221.intregratedapi.utils;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;
public class ListMapper {
    private static final ListMapper listMapper = new ListMapper();
    private final ModelMapper modelMapper;

    @Autowired
    public ListMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }
    public static ListMapper getInstance() {
        return listMapper;
    }
    public <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
        return source.stream()
                .map(entity -> modelMapper.map(entity, targetClass))
                .collect(Collectors.toList());
    }


}
