package com.study.platform.service;

import com.study.platform.entity.Course;
import com.study.platform.entity.CourseSelection;
import com.study.platform.repository.CourseRepository;
import com.study.platform.repository.CourseSelectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseSelectionService {

    private final CourseSelectionRepository selectionRepository;
    private final CourseRepository courseRepository;

    public CourseSelectionService(CourseSelectionRepository selectionRepository,
                                  CourseRepository courseRepository) {
        this.selectionRepository = selectionRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public void select(Long courseId, Long studentId) {
        // 选课：校验课程存在、未重复选、还有名额，然后扣减一个名额
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("课程不存在"));
        if (selectionRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new RuntimeException("已经选择过该课程");
        }
        if (course.getCapacity() == null || course.getCapacity() <= 0) {
            throw new RuntimeException("该课程名额已满");
        }
        CourseSelection selection = new CourseSelection();
        selection.setStudentId(studentId);
        selection.setCourseId(courseId);
        selectionRepository.save(selection);
        course.setCapacity(course.getCapacity() - 1);
        courseRepository.save(course);
    }

    @Transactional
    public void deselect(Long courseId, Long studentId) {
        // 退选：删除选课记录并加回一个名额
        if (!selectionRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            return;
        }
        selectionRepository.deleteByStudentIdAndCourseId(studentId, courseId);
        courseRepository.findById(courseId).ifPresent(c -> {
            c.setCapacity((c.getCapacity() == null ? 0 : c.getCapacity()) + 1);
            courseRepository.save(c);
        });
    }

    public List<Course> myCourses(Long studentId) {
        // 返回该学生已选课程的完整信息
        List<Long> ids = selectionRepository.findByStudentId(studentId).stream()
                .map(CourseSelection::getCourseId)
                .collect(Collectors.toList());
        if (ids.isEmpty()) return List.of();
        return courseRepository.findAllById(ids);
    }

    public List<Long> myCourseIds(Long studentId) {
        // 只返回已选课程的 id 列表，供前端标记“已选”状态
        return selectionRepository.findByStudentId(studentId).stream()
                .map(CourseSelection::getCourseId)
                .collect(Collectors.toList());
    }
}
