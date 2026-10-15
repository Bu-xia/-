package com.study.platform.config;

import com.study.platform.entity.Course;
import com.study.platform.entity.User;
import com.study.platform.repository.CourseRepository;
import com.study.platform.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    // 应用启动后初始化：创建默认管理员和示例课程（仅当不存在时）
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public DataSeeder(UserRepository userRepository, CourseRepository courseRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public void run(String... args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(encoder.encode("admin123"));
            admin.setName("管理员");
            admin.setRole("admin");
            userRepository.save(admin);
        }
        if (courseRepository.count() == 0) {
            Course c1 = new Course();
            c1.setCode("CS101");
            c1.setName("高等数学");
            c1.setTeacher("张老师");
            c1.setDescription("大学公共基础课程");
            c1.setSchedule("周一 10:00-12:00");
            c1.setCapacity(40);
            courseRepository.save(c1);

            Course c2 = new Course();
            c2.setCode("CS102");
            c2.setName("大学英语");
            c2.setTeacher("李老师");
            c2.setDescription("大学公共基础课程");
            c2.setSchedule("周三 14:00-16:00");
            c2.setCapacity(30);
            courseRepository.save(c2);
        }
    }
}
