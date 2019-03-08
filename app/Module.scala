import com.google.inject.AbstractModule
import repo.{LeagueRepository, LeagueRepositoryImpl}

class Module extends AbstractModule {

  override def configure() = {
    bind(classOf[LeagueRepository]).to(classOf[LeagueRepositoryImpl]).asEagerSingleton()
  }
}