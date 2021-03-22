package br.com.caelum.pm73.dao.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.pm73.dao.CriadorDeSessao;
import br.com.caelum.pm73.dao.LeilaoDao;
import br.com.caelum.pm73.dao.UsuarioDao;
import br.com.caelum.pm73.dominio.Leilao;
import br.com.caelum.pm73.dominio.Usuario;

public class LeilaoDaoTest {

	private Session session;
	private UsuarioDao usuarioDao;
	private LeilaoDao leilaoDao;
	private Usuario usuario;
	private Leilao leilaoUm;
	private Leilao leilaoDois;
	private Leilao leilaoTres;

	@Before
	public void setUp() {
		this.session = new CriadorDeSessao().getSession();
		this.usuarioDao = new UsuarioDao(session);
		this.leilaoDao = new LeilaoDao(session);
		this.usuario = new Usuario("Thiago", "thiago@gmail.com");
		this.leilaoUm = new Leilao("Carro", 15000.0, usuario, false);
		this.leilaoDois = new Leilao("Geladeira", 1500.0, usuario, true);
		this.leilaoTres = new Leilao("Moto", 25500.0, usuario, false);
		session.beginTransaction();
	}

	@After
	public void closeSetUp() {
		session.getTransaction().rollback();
		session.close();
	}

	@Test
	public void deveContarLeiloesNaoEncerrados() {
		Leilao leilaoAtivo = new Leilao("Carro", 15000.0, usuario, false);
		Leilao leilaoEncerrado = new Leilao("Geladeira", 1500.0, usuario, true);
		leilaoEncerrado.encerra();

		usuarioDao.salvar(usuario);
		leilaoDao.salvar(leilaoAtivo);
		leilaoDao.salvar(leilaoEncerrado);

		long total = leilaoDao.total();

		assertEquals(1L, total);
	}

	@Test
	public void deveRetornarZeroCasoNaoHouveNenhumLeilaoEncerrado() {
		leilaoUm.encerra();
		leilaoDois.encerra();

		usuarioDao.salvar(usuario);
		leilaoDao.salvar(leilaoUm);
		leilaoDao.salvar(leilaoDois);

		long total = leilaoDao.total();
		assertEquals(0l, total);
	}

	@Test
	public void retornaApenasLeiloesNaoUsados() {
		usuarioDao.salvar(usuario);
		leilaoDao.salvar(leilaoUm);
		leilaoDao.salvar(leilaoDois);
		leilaoDao.salvar(leilaoTres);

		List<Leilao> novos = leilaoDao.novos();
		assertEquals(2L, novos.size());
	}

	@Test
	public void retornaLeiloesCriadosHaMaisDeUmaSemanaAtras() {
		Calendar dataAntiga = Calendar.getInstance();
		dataAntiga.add(Calendar.DAY_OF_MONTH, -8);
		leilaoUm.setDataAbertura(dataAntiga);

		leilaoDao.salvar(leilaoUm);
		leilaoDao.salvar(leilaoDois);
		leilaoDao.salvar(leilaoTres);

		usuarioDao.salvar(usuario);

		List<Leilao> antigos = leilaoDao.antigos();
		assertEquals(1L, antigos.size());
	}

	@Test
	public void retornaLeiloesCriadosExatamentesUmaSemanaAtras() {
		Calendar dataSetediasAtras = Calendar.getInstance();
		dataSetediasAtras.add(Calendar.DAY_OF_MONTH, -7);

		leilaoUm.setDataAbertura(dataSetediasAtras);
		leilaoDois.setDataAbertura(dataSetediasAtras);

		leilaoDao.salvar(leilaoUm);
		leilaoDao.salvar(leilaoDois);
		leilaoDao.salvar(leilaoTres);

		usuarioDao.salvar(usuario);

		List<Leilao> antigos = leilaoDao.antigos();

		List<Leilao> antigosExatosSeteDiasAtras = new ArrayList<Leilao>();

		for (Leilao leilao : antigos) {
			if (leilao.getDataAbertura().equals(dataSetediasAtras)) {
				antigosExatosSeteDiasAtras.add(leilao);
			}
		}

		assertEquals(2L, antigos.size());
	}

}
