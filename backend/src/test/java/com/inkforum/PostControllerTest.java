package com.inkforum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkforum.dto.PostRequest;
import com.inkforum.dto.PostResponse;
import com.inkforum.entity.User;
import com.inkforum.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 帖子 CRUD 端到端集成测试（H2 内存数据库）
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private Long testUserId;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@inkforum.com");
        user.setPassword("hashed_password");
        testUserId = userRepository.save(user).getId();
    }

    // ─────────── CREATE ───────────

    @Test
    @DisplayName("POST /api/posts — 成功创建帖子，返回201")
    void createPost_success() throws Exception {
        PostRequest req = buildRequest("红楼梦第一回赏析", "此回开篇…", "诗词鉴赏");

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("红楼梦第一回赏析"))
                .andExpect(jsonPath("$.category").value("诗词鉴赏"))
                .andExpect(jsonPath("$.authorName").value("testuser"));
    }

    @Test
    @DisplayName("POST /api/posts — 标题为空时返回400")
    void createPost_blankTitle_returns400() throws Exception {
        PostRequest req = buildRequest("", "内容", "分类");

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/posts — 作者不存在时返回404")
    void createPost_authorNotFound_returns404() throws Exception {
        PostRequest req = buildRequest("测试帖子", "测试内容", null);
        req.setAuthorId(9999L);

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ─────────── READ ───────────

    @Test
    @DisplayName("GET /api/posts — 返回分页帖子列表")
    void getAllPosts_returnsList() throws Exception {
        createViaApi("帖子一", "内容一", "小说推荐");
        createViaApi("帖子二", "内容二", "小说推荐");

        mockMvc.perform(get("/api/posts").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("GET /api/posts/{id} — 返回单篇帖子并递增浏览次数")
    void getPostById_incrementsViewCount() throws Exception {
        PostResponse created = createViaApi("浏览测试帖", "内容", null);

        MvcResult result = mockMvc.perform(get("/api/posts/{id}", created.getId()))
                .andExpect(status().isOk())
                .andReturn();

        PostResponse resp = objectMapper.readValue(
                result.getResponse().getContentAsString(), PostResponse.class);
        assertThat(resp.getViewCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("GET /api/posts/{id} — 不存在时返回404")
    void getPostById_notFound() throws Exception {
        mockMvc.perform(get("/api/posts/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/posts/search — 按关键字搜索")
    void searchByTitle() throws Exception {
        createViaApi("红楼梦赏析", "内容", "诗词鉴赏");
        createViaApi("三国演义讨论", "内容", "小说推荐");

        mockMvc.perform(get("/api/posts/search").param("keyword", "红楼"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("红楼梦赏析"));
    }

    // ─────────── UPDATE ───────────

    @Test
    @DisplayName("PUT /api/posts/{id} — 成功更新帖子")
    void updatePost_success() throws Exception {
        PostResponse created = createViaApi("原标题", "原内容", "分类A");

        PostRequest updateReq = buildRequest("新标题", "新内容", "分类B");

        mockMvc.perform(put("/api/posts/{id}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("新标题"))
                .andExpect(jsonPath("$.category").value("分类B"));
    }

    @Test
    @DisplayName("PUT /api/posts/{id} — 帖子不存在时返回404")
    void updatePost_notFound() throws Exception {
        PostRequest req = buildRequest("标题", "内容", null);

        mockMvc.perform(put("/api/posts/{id}", 9999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ─────────── DELETE ───────────

    @Test
    @DisplayName("DELETE /api/posts/{id} — 成功删除，返回204")
    void deletePost_success() throws Exception {
        PostResponse created = createViaApi("待删帖子", "内容", null);

        mockMvc.perform(delete("/api/posts/{id}", created.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/posts/{id}", created.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/posts/{id} — 不存在时返回404")
    void deletePost_notFound() throws Exception {
        mockMvc.perform(delete("/api/posts/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    // ─────────── STATS ───────────

    @Test
    @DisplayName("GET /api/stats — 返回统计数据")
    void getStats() throws Exception {
        createViaApi("帖子A", "内容", null);

        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberCount").value(1))
                .andExpect(jsonPath("$.postCount").value(1))
                .andExpect(jsonPath("$.commentCount").value(0));
    }

    // ─────────── Helpers ───────────

    private PostRequest buildRequest(String title, String content, String category) {
        PostRequest req = new PostRequest();
        req.setTitle(title);
        req.setContent(content);
        req.setCategory(category);
        req.setAuthorId(testUserId);
        return req;
    }

    private PostResponse createViaApi(String title, String content, String category) throws Exception {
        PostRequest req = buildRequest(title, content, category);
        MvcResult result = mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), PostResponse.class);
    }
}
