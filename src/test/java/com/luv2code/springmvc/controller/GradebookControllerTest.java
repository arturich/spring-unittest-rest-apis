package com.luv2code.springmvc.controller;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luv2code.springmvc.models.CollegeStudent;
import com.luv2code.springmvc.repository.HistoryGradesDao;
import com.luv2code.springmvc.repository.MathGradesDao;
import com.luv2code.springmvc.repository.ScienceGradesDao;
import com.luv2code.springmvc.repository.StudentDao;
import com.luv2code.springmvc.service.StudentAndGradeService;

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
    @DisplayName("Get Students Http Request")
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
    
    @Test
    @DisplayName("Delete Student Rest")
    public void deleteStudentHttpRequest() throws Exception
    {
    	//Check if the student is in the DB
    	
    	Optional<CollegeStudent> verifyStudent = studentDao.findById(1);
    	
    	assertTrue(verifyStudent.isPresent(),"Student should exist before delete");
    	
    	mockMvc.perform(MockMvcRequestBuilders.delete("/student/{id}",1))
    	.andExpect(status().isOk())
    	.andExpect(content().contentType(APPLICATION_JSON_UTF8))
    	.andExpect(jsonPath("$",hasSize(0)));
    	
    	verifyStudent = studentDao.findById(1);
    	
    	assertFalse(verifyStudent.isPresent());
    	
    }
    
    @Test
    @DisplayName("Delete a non-existing student")
    public void tryToDeleteANonExistingStudent() throws Exception
    {
    	//student should not exist
    	assertFalse(studentDao.findById(0).isPresent(),"Student should NOT exist");
    	
    	mockMvc.perform(MockMvcRequestBuilders.delete("/student/{id}",0))
    		.andExpect(status().is4xxClientError())
    		.andExpect(content().contentType(APPLICATION_JSON_UTF8))
    		.andExpect(jsonPath("$.status",is(404)))
    		.andExpect(jsonPath("$.message",is("Student or Grade was not found")))
    		;
    }
    
    @Test
    @DisplayName("Get Student Information")
    public void getStudentInformation() throws Exception
    {
    	// Student must exist before trying to retrieve information
    	assertTrue(studentDao.findById(1).isPresent(),"Student should exist before trying to get his info");
    	
    	mockMvc.perform(MockMvcRequestBuilders.get("/studentInformation/{id}",1))
    			.andExpect(status().isOk())
    			.andExpect(content().contentType(APPLICATION_JSON_UTF8))  
    			.andExpect(jsonPath("$.id",is(1)))
    			.andExpect(jsonPath("$.firstname",is("Eric")))
    			.andExpect(jsonPath("$.lastname",is("Roby")))
    			.andExpect(jsonPath("$.emailAddress",is("eric.roby@luv2code_school.com")));
    			
    	
    }
    
    @Test
    @DisplayName("Try to retrieve student info o invalid student")
    public void tryToGetStudentInfoOnInvalidStudent() throws Exception
    {
    	assertFalse(studentDao.findById(0).isPresent(),"Student should not exist");
    	
    	mockMvc.perform(MockMvcRequestBuilders.get("/studentInformation/{id}",0))
    	.andExpect(status().is4xxClientError())
    	.andExpect(content().contentType(APPLICATION_JSON_UTF8))
    	.andExpect(jsonPath("$.status",is(404)))
		.andExpect(jsonPath("$.message",is("Student or Grade was not found")));    
    	
    }
    
    @Test
    @DisplayName("Create student grade")
    public void createStudentGrade() throws Exception
    {
    	Optional<CollegeStudent> verifyStudent = studentDao.findById(1);
    	
    	//Check that student exist
    	assertTrue(verifyStudent.isPresent(),"Student should exist");
    	
    	mockMvc.perform(MockMvcRequestBuilders.post("/grades")
    			.contentType(APPLICATION_JSON_UTF8)
    			.param("grade", "100")
    			.param("gradeType","math" )
    			.param("studentId", "1")    			
    			)
    		.andExpect(status().isOk())
    		.andExpect(content().contentType(APPLICATION_JSON_UTF8))
    		.andExpect(jsonPath("$.id",is(1)))
    		.andExpect(jsonPath("$.firstname",is("Eric")))
    		.andExpect(jsonPath("$.lastname",is("Roby")))
    		.andExpect(jsonPath("$.emailAddress",is("eric.roby@luv2code_school.com")))
    		.andExpect(jsonPath("$.studentGrades.mathGradeResults",hasSize(2)));
    	
    	//check that the grade was added
    }
    
    @Test
    @DisplayName("Create grade for student that does not exist")
    public void createGradeForInvalidStudent() throws Exception
    {
    	assertFalse(studentDao.findById(0).isPresent(),"Student must not exist");
    	
    	mockMvc.perform(MockMvcRequestBuilders.post("/grades")
    			.contentType(APPLICATION_JSON_UTF8)
    			.param("grade", "80.56")
    			.param("gradeType", "math")
    			.param("studentId", "0")    			
    			)
    				.andExpect(status().is4xxClientError())
    				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
    				.andExpect(jsonPath("$.status",is(404)))
    				.andExpect(jsonPath("$.message",is("Student or Grade was not found")));
    			
    	
    }
    
    @Test
    @DisplayName("Create a grade for a invalid subject")
    public void createGradeOnInvalidSubject() throws Exception
    {
    	
    	mockMvc.perform(MockMvcRequestBuilders.post("/grades")
    			.contentType(APPLICATION_JSON_UTF8)
    			.param("grade", "85.5")
    			.param("gradeType", "literature")
    			.param("studentId","1")    			
    			)
    				.andExpect(status().is4xxClientError())
    				.andExpect(content().contentType(APPLICATION_JSON_UTF8))
    				.andExpect(jsonPath("$.status",is(404)))
    				.andExpect(jsonPath("$.message", is("Student or Grade was not found")));
    				
    	
    }
    
    @Test
    @DisplayName("Delete a grade")
    public void deleteAGrade() throws Exception
    {
    	//check that the grade exist
    	assertTrue(mathGradeDao.existsById(1),"Check that the grade exist");
    	
    	mockMvc.perform(MockMvcRequestBuilders.delete("/grades/{id}/{gradeType}",1,"math"))
    	.andExpect(status().isOk())
    	.andExpect(jsonPath("$.id",is(1)))
    	.andExpect(jsonPath("$.firstname",is("Eric")))
    	.andExpect(jsonPath("$.lastname",is("Roby")))
    	.andExpect(jsonPath("$.emailAddress",is("eric.roby@luv2code_school.com")))
    	.andExpect(jsonPath("$.studentGrades.mathGradeResults",hasSize(0)));
    	
    	
    	//check that the grade does NOT exist
    	assertFalse(mathGradeDao.existsById(1),"Check that the grade does NOT exist");
    }
    
    @Test
    @DisplayName("Delete a grade on invalid grade id")
    public void deleteAgradeOnInvalidGradeId() throws Exception
    {
    	//Check that grade id does not exist
    	assertFalse(mathGradeDao.existsById(0),"The id must not exist");    	
    	
    	mockMvc.perform(MockMvcRequestBuilders.delete("/grades/{id}/{gradeType}",0,"math"))
    		.andExpect(status().is4xxClientError())
    		.andExpect(content().contentType(APPLICATION_JSON_UTF8))
    		.andExpect(jsonPath("$.status",is(404)))
    		.andExpect(jsonPath("$.message", is("Student or Grade was not found")));
    	

    }
    
    

    
    @AfterEach
    public void setupAfterTransaction() {
        jdbc.execute(sqlDeleteStudent);
        jdbc.execute(sqlDeleteMathGrade);
        jdbc.execute(sqlDeleteScienceGrade);
        jdbc.execute(sqlDeleteHistoryGrade);
    }

}
