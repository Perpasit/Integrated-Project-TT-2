package sit.int221.intregratedapi.utils;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import sit.int221.intregratedapi.dtos.TaskDTO;
import sit.int221.intregratedapi.entities.Task;

import java.util.List;
import java.util.stream.Collectors;

public class ListMapper {
    private static final ModelMapper _modelMapper_;

    static {
        _modelMapper_ = new ModelMapper();
        _modelMapper_.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setSkipNullEnabled(false);

        // Add mappings for title and assignees
        _modelMapper_.addMappings(new PropertyMap<Task, TaskDTO>() {
            @Override
            protected void configure() {
                map().setTitle(source.getTaskTitle());
                map().setAssignees(source.getTaskAssignees());
            }
        });
    }

    public ListMapper(ModelMapper modelMapper) {
    }

    public static <S, T> List<T> mapList(List<S> source, Class<T> targetClass) {
        return source.stream()
                .map(entity -> _modelMapper_.map(entity, targetClass))
                .collect(Collectors.toList());
    }

    public static <S, T> T mapObject(S source, Class<T> targetClass) {
        return _modelMapper_.map(source, targetClass);
    }
}