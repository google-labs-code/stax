/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.planck.planck.domain.project;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planck.planck.base.IntegrationTestBase;
import com.planck.planck.domain.project.dto.CreateProjectCommand;
import com.planck.planck.domain.project.dto.ProjectDTO;
import com.planck.planck.domain.user.UserRepository;
import com.planck.planck.entitities.Project;
import com.planck.planck.entitities.User;
import com.planck.planck.enums.EvaluationType;
import com.planck.planck.util.PlanckConstants;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class ProjectControllerTestIT extends IntegrationTestBase {

  @Autowired private ProjectRepository projectRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private ProjectService projectService;
  @Autowired private ObjectMapper objectMapper;

  private User testUser;

  @BeforeEach
  public void setUp() {
    super.setUp();
    testUser = userRepository.findByEmail(PlanckConstants.DEFAULT_USER).orElseThrow();
  }

  @Test
  void listProjects_shouldReturnProjectsForUser() throws Exception {
    projectRepository.save(
        Project.builder()
            .user(testUser)
            .name("Project A")
            .evaluationType(EvaluationType.POINTWISE)
            .build());
    projectRepository.save(
        Project.builder()
            .user(testUser)
            .name("Project B")
            .evaluationType(EvaluationType.POINTWISE)
            .build());

    // Use mockMvc.perform(get) and jsonPath for assertions
    mockMvc
        .perform(get("/projects").header("Authorization", getBearerJwtToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.projects.size()", equalTo(2)))
        // Assuming the order is descending by creation time (B then A), as implied by the
        // RestAssured test
        .andExpect(jsonPath("$.projects[0].name", equalTo("Project B")))
        .andExpect(jsonPath("$.projects[1].name", equalTo("Project A")));
  }

  @Test
  void getProject_shouldReturnProjectById() throws Exception {
    Project project =
        projectRepository.save(Project.builder().user(testUser).name("Test Project").build());

    mockMvc
        .perform(
            get("/projects/{projectId}", project.getId())
                .header("Authorization", getBearerJwtToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name", equalTo("Test Project")))
        .andExpect(jsonPath("$.project_id", equalTo(project.getId())));
  }

  @Test
  void getProject_shouldReturnNotFound_ifProjectDoesNotExist() throws Exception {
    String nonExistentId = UUID.randomUUID().toString();
    mockMvc
        .perform(
            get("/projects/{projectId}", nonExistentId)
                .header("Authorization", getBearerJwtToken()))
        .andExpect(status().isNotFound());
  }

  @Test
  void createProject_shouldReturnCreatedProject() throws Exception {
    ProjectDTO projectDTO =
        ProjectDTO.builder()
            .name("New Project")
            .type(EvaluationType.POINTWISE)
            .description("A description")
            .build();

    // Use mockMvc.perform(post) and serialize the DTO
    mockMvc
        .perform(
            post("/projects")
                .header("Authorization", getBearerJwtToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name", equalTo("New Project")))
        .andExpect(jsonPath("$.description", equalTo("A description")))
        .andExpect(jsonPath("$.type", equalTo("POINTWISE")))
        .andExpect(jsonPath("$.project_id", notNullValue()));
  }

  @Test
  void deleteProject_shouldReturnSuccessMessage() throws Exception {
    Project project =
        projectRepository.save(Project.builder().user(testUser).name("To Delete").build());

    mockMvc
        .perform(
            delete("/projects/{projectId}", project.getId())
                .header("Authorization", getBearerJwtToken()))
        .andExpect(status().isOk())
        // Note: Use the project ID directly in the expected message
        .andExpect(jsonPath("$.message", equalTo(project.getId() + " was deleted successfully")));
  }

  @Test
  void updateProject_shouldReturnUpdatedProject() throws Exception {
    Project project =
        projectRepository.save(Project.builder().user(testUser).name("Original Name").build());
    ProjectDTO updateDTO =
        ProjectDTO.builder().name("Updated Name").description("Updated description").build();

    // Use mockMvc.perform(patch) and serialize the DTO
    mockMvc
        .perform(
            patch("/projects/{projectId}", project.getId())
                .header("Authorization", getBearerJwtToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name", equalTo("Updated Name")))
        .andExpect(jsonPath("$.description", equalTo("Updated description")));
  }

  @Test
  void exportChats_shouldReturnOkAndListOfChats() throws Exception {
    ProjectDTO project =
        projectService.createProject(
            testUser,
            new CreateProjectCommand(
                "export-project",
                "Export Project",
                "Description",
                false,
                EvaluationType.POINTWISE));

    mockMvc
        .perform(
            get("/projects/{projectId}/export", project.getProjectId())
                .header("Authorization", getBearerJwtToken()))
        .andExpect(status().isOk());
  }

  @Test
  void getProjectMetricsSummary_shouldReturnSummary() throws Exception {
    Project project =
        projectRepository.save(Project.builder().user(testUser).name("Metrics Project").build());

    mockMvc
        .perform(
            get("/projects/{projectId}/metrics-summary", project.getId())
                .header("Authorization", getBearerJwtToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", notNullValue()));
  }

  @Test
  void getHumanEvalPassRate_shouldReturnMetrics() throws Exception {
    Project project =
        projectRepository.save(Project.builder().user(testUser).name("Human Eval Project").build());

    mockMvc
        .perform(
            get("/projects/{projectId}/human-eval-pass-rate", project.getId())
                .header("Authorization", getBearerJwtToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", notNullValue()));
  }

  @Test
  void getProjectEvaluationAnalyticsByScorer_shouldReturnAnalytics() throws Exception {
    Project project =
        projectRepository.save(Project.builder().user(testUser).name("Analytics Project").build());
    String scorerId = testUser.getId();

    mockMvc
        .perform(
            get("/projects/{projectId}/eval-analytics/{scorerId}", project.getId(), scorerId)
                .header("Authorization", getBearerJwtToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", notNullValue()));
  }

  @Test
  void getProjectEvaluationAnalyticsForAllScorers_shouldReturnAnalyticsList() throws Exception {
    Project project =
        projectRepository.save(
            Project.builder().user(testUser).name("Analytics Project All Scorers").build());

    mockMvc
        .perform(
            get("/projects/{projectId}/eval-analytics", project.getId())
                .header("Authorization", getBearerJwtToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", notNullValue()));
  }

  @Test
  void importChats_shouldReturnImportResult() throws Exception {
    ProjectDTO project =
        projectService.createProject(
            testUser,
            new CreateProjectCommand(
                "import-project",
                "Import Project",
                "Description",
                false,
                EvaluationType.POINTWISE));

    String csvContent =
        """
                        input,turns,human_evaluation,inference_analytics,chat,model_nickname,output
                        Hi,1,0,"{""total_chat_latency"":0,""average_chat_latency"":0,""total_chat_prompt_tokens"":0,""total_chat_completion_tokens"":0,""total_chat_tokens"":0}","[{""role"":""user"",""content"":""Hi""}]",,
                        Hi,1,0,"{""total_chat_latency"":1792,""average_chat_latency"":1792,""total_chat_prompt_tokens"":2,""total_chat_completion_tokens"":20,""total_chat_tokens"":22}","[{""role"":""user"",""content"":""Hi""},{""role"":""assistant"",""content"":""Hi there! \uD83D\uDE0A \\n\\nHow can I help you today? Let me know what you're thinking about or what you need.""}]",Gemma 3 12B,"Hi there! \uD83D\uDE0A\\n\\nHow can I help you today? Let me know what you're thinking about or what you need."
                        """;

    MockMultipartFile mockFile =
        new MockMultipartFile("file", "test.csv", "text/csv", csvContent.getBytes());

    mockMvc
        .perform(
            multipart("/projects/{projectId}/import", project.getProjectId())
                .file(mockFile)
                .header("Authorization", getBearerJwtToken())
                .param("chat_column_name", "chat"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.successfulRows", equalTo(2)));
  }

  @Test
  void importChats_shouldReturnBadRequest_whenFileIsEmpty() throws Exception {
    ProjectDTO project =
        projectService.createProject(
            testUser,
            new CreateProjectCommand(
                "empty-file-project",
                "Empty File Project",
                "Description",
                false,
                EvaluationType.POINTWISE));

    MockMultipartFile emptyMockFile =
        new MockMultipartFile("file", "empty.csv", "text/csv", "".getBytes());

    mockMvc
        .perform(
            multipart("/projects/{projectId}/import", project.getProjectId())
                .file(emptyMockFile)
                .header("Authorization", getBearerJwtToken())
                .param("chat_column_name", "chat"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.successfulRows", equalTo(0)));
  }

  @Test
  void importChats_shouldReturnInternalServerError_whenTooManyVariablesColumnsProvided()
      throws Exception {
    ProjectDTO project =
        projectService.createProject(
            testUser,
            new CreateProjectCommand(
                "too-many-variables-project",
                "Too Many Variables Project",
                "Description",
                false,
                EvaluationType.POINTWISE));

    String fileContent = "header1,header2\ndata1,data2";
    MockMultipartFile mockFile =
        new MockMultipartFile("file", "valid.csv", "text/csv", fileContent.getBytes());

    String tooManyVariablesColumns =
        "meta1,meta2,meta3,meta4,meta5,meta6,meta7,meta8,meta9,meta10,meta11";

    mockMvc
        .perform(
            multipart("/projects/{projectId}/import", project.getProjectId())
                .file(mockFile)
                .header("Authorization", getBearerJwtToken())
                .param("chat_column_name", "chat")
                .param("variables_column_names", tooManyVariablesColumns))
        .andExpect(status().isInternalServerError())
        .andExpect(
            MockMvcResultMatchers.content()
                .string(containsString("The number of variables columns cannot exceed 10.")));
  }

  @Test
  void importChats_shouldReturnImportResult_whenVariablesColumnNamesAreEmpty() throws Exception {
    ProjectDTO project =
        projectService.createProject(
            testUser,
            new CreateProjectCommand(
                "empty-variables-project",
                "Empty Variables Project",
                "Description",
                false,
                EvaluationType.POINTWISE));

    String csvContent =
        """
                    input,chat
                    Hi,"[{""role"":""user"",""content"":""Hi""}]"
                    Bye,"[{""role"":""user"",""content"":""Bye""}]"
                    """;

    MockMultipartFile mockFile =
        new MockMultipartFile("file", "empty_vars.csv", "text/csv", csvContent.getBytes());

    mockMvc
        .perform(
            multipart("/projects/{projectId}/import", project.getProjectId())
                .file(mockFile)
                .header("Authorization", getBearerJwtToken())
                .param("chat_column_name", "chat")
                .param("variables_column_names", ""))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.successfulRows", equalTo(2)));
  }

  @Test
  void importChats_shouldReturnImportResult_whenVariablesColumnNamesAreBlank() throws Exception {
    ProjectDTO project =
        projectService.createProject(
            testUser,
            new CreateProjectCommand(
                "blank-variables-project",
                "Blank Variables Project",
                "Description",
                false,
                EvaluationType.POINTWISE));

    String csvContent =
        """
                    input,chat
                    Hi,"[{""role"":""user"",""content"":""Hi""}]"
                    Bye,"[{""role"":""user"",""content"":""Bye""}]"
                    """;

    MockMultipartFile mockFile =
        new MockMultipartFile("file", "blank_vars.csv", "text/csv", csvContent.getBytes());

    mockMvc
        .perform(
            multipart("/projects/{projectId}/import", project.getProjectId())
                .file(mockFile)
                .header("Authorization", getBearerJwtToken())
                .param("chat_column_name", "chat")
                .param("variables_column_names", "   "))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.successfulRows", equalTo(2)));
  }
}
