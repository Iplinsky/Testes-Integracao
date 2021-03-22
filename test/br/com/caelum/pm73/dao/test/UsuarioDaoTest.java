package br.com.caelum.pm73.dao.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.pm73.dao.CriadorDeSessao;
import br.com.caelum.pm73.dao.UsuarioDao;
import br.com.caelum.pm73.dominio.Usuario;

public class UsuarioDaoTest {

	private Session session;
	private UsuarioDao usuarioDao;

	@Before
	public void setUp() {
		this.session = new CriadorDeSessao().getSession();
		this.usuarioDao = new UsuarioDao(session);
		session.beginTransaction();
	}

	@After
	public void closeSetUp() {
		session.getTransaction().rollback();
		session.close();
	}

	@Test
	public void deveEncontrarPeloNomeEEmail() {
		Usuario novoUsuario = new Usuario("Thiago", "thiago@gmail.com");
		usuarioDao.salvar(novoUsuario);
		Usuario usuarioLocalizado = usuarioDao.porNomeEEmail(novoUsuario.getNome(), novoUsuario.getEmail());

		assertEquals(novoUsuario, usuarioLocalizado);

	}

	@Test
	public void deveRetornarNullAoBuscarPeloNomeEEmail() {
		Usuario usuarioDoBanco = usuarioDao.porNomeEEmail("Jo�o Joaquim", "joao@joaquim.com.br");
		assertNull(usuarioDoBanco);
	}

}
