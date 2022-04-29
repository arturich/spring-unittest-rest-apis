package com.luv2code.springmvc.controller;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.assertj.core.error.ShouldHaveSameSizeAs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luv2code.springmvc.models.CollegeStudent;
import com.luv2code.springmvc.repository.HistoryGradesDao;
import com.luv2code.springmvc.repository.MathGradesDao;
import com.luv2code.springmvc.repository.ScienceGradesDao;
import com.luv2code.springmvc.repository.StudentDao;
import com.luv2code.springmvc.service.StudentAndGradeService;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.Simple.class)
@Transactional
class GradebookControllerTest {
	
	private static MockHttpServletRequest request;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Mock
	StudentAndGradeService studentCreateServiceMock;
	
	@Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private MathGradesDao mathGradeDao;

    @Autowired
    private ScienceGradesDao scienceGradeDao;

    @Autowired
    private HistoryGradesDao historyGradeDao;

    @Autowired
    private StudentAndGradeService studentService;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    ObjectMapper objectMapper;
    
    @Autowired
    private CollegeStudent student;
    
    
    @Value("${sql.script.create.student}")
    private String sqlAddStudent;

    @Value("${sql.script.create.math.grade}")
    private String sqlAddMathGrade;

    @Value("${sql.script.create.science.grade}")
    private String sqlAddScienceGrade;

    @Value("${sql.script.create.history.grade}")
    private String sqlAddHistoryGrade;

    @Value("${sql.script.delete.student}")
    private String sqlDeleteStudent;

    @Value("${sql.script.delete.math.grade}")
    private String sqlDeleteMathGrade;

    @Value("${sql.script.delete.science.grade}")
    private String sqlDeleteScienceGrade;

    @Value("${sql.script.delete.history.grade}")
    private String sqlDeleteHistoryGrade;
    
    public static final MediaType APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON;
    
    
    @BeforeAll
    public static void setup()
    {
    	request = new MockHttpServletRequest();
    	
    	request.setParameter("firstname", "arturich");
    	
    	request.setParameter("lastname", "kings");
    	
    	request.setParameter("email", "arturich@mail.com");
    	
    }
    
    @BeforeEach
    public void setupDatabase() {
        jdbc.execute(sqlAddStudent);
        jdbc.execute(sqlAddMathGrade);
        jdbc.execute(sqlAddScienceGrade);
        jdbc.execute(sqlAddHistoryGrade);
    }
    
    @Test
    @DisplayName("Create Students Http Request")
    public void getStudentsHtppRequest() throws Exception
    {
    	student.setEmailAddress("mail@mail.com");
    	student.setFirstname("Will");
    	student.setLastname("Morales");
    	
    	entityManager.persist(student);
    	entityManager.flush();
    	
    	mockMvc.perform(MockMvcRequestBuilders.get("/"))
    	.andExpect(status().isOk())
    	.andExpect(content().contentType(APPLICATION_JSON_UTF8))
    	.andExpect(jsonPath("$",hasSize(2)));
    }
    
    @Test
    @DisplayName("Create Student")
    public void createStudentHttpRequest() throws Exception
    {
    	student.setEmailAddress("mail@mail.com");
    	student.setFirstname("Will");
    	student.setLastname("Morales");
    	
    	mockMvc.perform(MockMvcRequestBuilders.post("/")
    			.contentType(APPLICATION_JSON_UTF8)
    			.content(objectMapper.writeValueAsString(student))) //this is from Jackson Api generationg the JSON
    			.andExpect(status().isOk())
    			.andExpect(jsonPath("$",hasSize(2)));   
    	
    	CollegeStudent verifyStudent = studentDao.findByEmailAddress("mail@mail.com");
    	assertNotNull(verifyStudent,"Student should exist on DB");
    	
    }
    

    
    @AfterEach
    public void setupAfterTransaction() {
        jdbc.execute(sqlDeleteStudent);
        jdbc.execute(sqlDeleteMathGrade);
        jdbc.execute(sqlDeleteScienceGrade);
        jdbc.execute(sqlDeleteHistoryGrade);
    }

}
