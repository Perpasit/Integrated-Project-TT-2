package sit.int221.integratedapi.utils;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import sit.int221.integratedapi.dtos.TaskDTO;
import sit.int221.integratedapi.entities.Task;

import java.util.List;
import java.util.stream.Collectors;

public class ListMapper {
    private static final ModelMapper modelMapper;

    static {
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setSkipNullEnabled(false);

        // Add mappings for title and assignees
        modelMapper.addMappings(new PropertyMap<Task, TaskDTO>() {
            @Override
            protected void configure() {
                map().setTitle(source.getTitle());
                map().setAssignees(source.getAssignees());
            }
        });
    }

    public ListMapper(ModelMapper modelMapper) {
    }

    public static <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
        return source.stream()
                .map(entity -> modelMapper.map(entity, targetClass))
                .collect(Collectors.toList());
    }

    public static <S, T> T mapObject(S source, Class<T> targetClass) {
        return modelMapper.map(source, targetClass);
    }
}