package sit.int221.integratedapi.repositories.primary;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sit.int221.integratedapi.entities.Task;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    // ค้นหาทาสก์ตามสถานะ
    List<Task> findByStatusId(Integer statusId);

    // ค้นหาทาสก์ตามสถานะและเรียงลำดับ
    List<Task> findByStatusIdIn(List<Integer> statusIds, Sort sort);

    // ค้นหาทาสก์ตามสถานะหรือชื่อสถานะและเรียงลำดับ
    List<Task> findByStatusIdInOrStatusNameIn(List<Integer> statusIds, List<String> statusNames, Sort sort);

    // นับจำนวนทาสก์ตามสถานะ
    int countByStatusId(Integer id);

    // ค้นหาทาสก์ตามบอร์ด
    List<Task> findByBoardId(Integer boardId);

    // ค้นหาทาสก์ตามบอร์ดและชื่อสถานะ และเรียงลำดับ
    List<Task> findByBoardIdAndStatusNameIn(Integer boardId, List<String> statusNames, Sort sort);

    // ค้นหาทาสก์ตามชื่อสถานะและบอร์ด และเรียงลำดับ
    List<Task> findByStatusNameInAndBoardId(List<String> statusNames, Integer boardId, Sort sort);

    // ค้นหาทาสก์ตามบอร์ดและเรียงลำดับ
    List<Task> findByBoardId(Integer boardId, Sort sort);

    List<Task> findFirstByBoardIdAndStatusNameIn(Integer boardId, List<String> statusNames, Sort sort);

}
