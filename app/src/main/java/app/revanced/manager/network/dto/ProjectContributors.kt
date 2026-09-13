package app.revanced.manager.network.dto

data class ProjectRepository(
    val name: String,
    val url: String,
    val contributors: List<ProjectContributor>,
)

data class ProjectContributor(
    val username: String,
    val avatarUrl: String,
)
