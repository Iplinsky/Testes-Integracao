package br.com.caelum.pm73.dao.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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

	@Before
	public void setUp() {
		this.session = new CriadorDeSessao().getSession();
		this.usuarioDao = new UsuarioDao(session);
		this.leilaoDao = new LeilaoDao(session);
		this.usuario = new Usuario("Thiago", "thiago@gmail.com");

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
		Leilao leilaoUm = new LeilaoBuilder().comNome("Carro").comValor(15000.0).comDono(usuario).constroi();
		Leilao leilaoDois = new LeilaoBuilder().comNome("Geladeira").comValor(1500.0).comDono(usuario).encerrado().constroi();

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
		Leilao leilaoUm = new LeilaoBuilder().comNome("Carro").comValor(15000.0).comDono(usuario).constroi();
		Leilao leilaoDois = new LeilaoBuilder().comNome("Geladeira").comValor(1500.0).comDono(usuario).encerrado()
				.constroi();
		Leilao leilaoTres = new LeilaoBuilder().comNome("Moto").comValor(25500.0).comDono(usuario).constroi();

		usuarioDao.salvar(usuario);
		leilaoDao.salvar(leilaoUm);
		leilaoDao.salvar(leilaoDois);
		leilaoDao.salvar(leilaoTres);

		List<Leilao> novos = leilaoDao.novos();
		assertEquals(3L, novos.size());
	}

	@Test
	public void retornaLeiloesCriadosHaMaisDeUmaSemanaAtras() {
		Leilao leilaoUm = new LeilaoBuilder().comNome("Carro").comValor(15000.0).comDono(usuario).constroi();
		Leilao leilaoDois = new LeilaoBuilder().comNome("Geladeira").comValor(1500.0).comDono(usuario).encerrado()
				.constroi();
		Leilao leilaoTres = new LeilaoBuilder().comNome("Moto").comValor(25500.0).comDono(usuario).constroi();

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
		Leilao leilaoUm = new LeilaoBuilder().comNome("Carro").comValor(15000.0).comDono(usuario).constroi();
		Leilao leilaoDois = new LeilaoBuilder().comNome("Geladeira").comValor(1500.0).comDono(usuario).encerrado()
				.constroi();
		Leilao leilaoTres = new LeilaoBuilder().comNome("Moto").comValor(25500.0).comDono(usuario).constroi();

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

	@Test
	public void deveTrazerLeiloesNaoEncerradosPorPeriodo() {
		Leilao leilaoUm = new LeilaoBuilder().comNome("Carro").comValor(15000.0).comDono(usuario).constroi();
		Leilao leilaoDois = new LeilaoBuilder().comNome("Geladeira").comValor(1500.0).comDono(usuario).encerrado()
				.constroi();
		Leilao leilaoTres = new LeilaoBuilder().comNome("Moto").comValor(25500.0).comDono(usuario).constroi();

		Calendar intervaloInicial = Calendar.getInstance();
		intervaloInicial.add(Calendar.DAY_OF_MONTH, -10);
		Calendar intervaloFinal = Calendar.getInstance();

		Calendar dataLeilaoUm = Calendar.getInstance();
		dataLeilaoUm.add(Calendar.DAY_OF_MONTH, -22);
		leilaoUm.setDataAbertura(dataLeilaoUm);

		Calendar dataLeilaoDois = Calendar.getInstance();
		dataLeilaoDois.add(Calendar.DAY_OF_MONTH, -4);
		leilaoDois.setDataAbertura(dataLeilaoDois);

		Calendar dataLeilaoTres = Calendar.getInstance();
		dataLeilaoTres.add(Calendar.DAY_OF_MONTH, -4);
		leilaoTres.setDataAbertura(dataLeilaoTres);

		leilaoDao.salvar(leilaoUm);
		leilaoDao.salvar(leilaoDois);
		leilaoDao.salvar(leilaoTres);

		usuarioDao.salvar(usuario);

		List<Leilao> porPeriodo = leilaoDao.porPeriodo(intervaloInicial, intervaloFinal);

		assertEquals(1L, porPeriodo.size());
	}

	@Test
	public void deveTrazerLeiloesEncerradosPorPeriodo() {
		Leilao leilaoUm = new LeilaoBuilder().comNome("Carro").comValor(15000.0).comDono(usuario).constroi();
		Leilao leilaoDois = new LeilaoBuilder().comNome("Geladeira").comValor(1500.0).comDono(usuario).encerrado()
				.constroi();
		Leilao leilaoTres = new LeilaoBuilder().comNome("Moto").comValor(25500.0).comDono(usuario).constroi();

		Calendar intervaloInicial = Calendar.getInstance();
		intervaloInicial.add(Calendar.DAY_OF_MONTH, -10);
		Calendar intervaloFinal = Calendar.getInstance();

		Calendar dataLeilaoUm = Calendar.getInstance();
		dataLeilaoUm.add(Calendar.DAY_OF_MONTH, -22);
		leilaoUm.setDataAbertura(dataLeilaoUm);

		Calendar dataLeilaoDois = Calendar.getInstance();
		dataLeilaoDois.add(Calendar.DAY_OF_MONTH, -4);
		leilaoDois.setDataAbertura(dataLeilaoDois);

		Calendar dataLeilaoTres = Calendar.getInstance();
		dataLeilaoTres.add(Calendar.DAY_OF_MONTH, -4);
		leilaoTres.setDataAbertura(dataLeilaoTres);

		leilaoDao.salvar(leilaoUm);
		leilaoDao.salvar(leilaoDois);
		leilaoDao.salvar(leilaoTres);

		usuarioDao.salvar(usuario);

		List<Leilao> porPeriodo = leilaoDao.porPeriodo(intervaloInicial, intervaloFinal);

		assertEquals(1L, porPeriodo.size());
	}

	@Test
	public void retornaListaDeLeiloesEmQueOUsuarioDeuLance() {

		Leilao leilaoUm = new LeilaoBuilder().comNome("Carro").comValor(15000.0).comDono(usuario).constroi();
		Leilao leilaoDois = new LeilaoBuilder().comNome("Geladeira").comValor(1500.0).comDono(usuario).encerrado()
				.constroi();
		Leilao leilaoTres = new LeilaoBuilder().comNome("Moto").comValor(25500.0).comDono(usuario).constroi();

		leilaoDao.salvar(leilaoUm);
		leilaoDao.salvar(leilaoDois);
		leilaoDao.salvar(leilaoTres);

		usuarioDao.salvar(usuario);

		List<Leilao> listaLeiloesDoUsuario = leilaoDao.listaLeiloesDoUsuario(usuario);

		assertEquals(0, listaLeiloesDoUsuario.size());

	}

	@Test
	public void deletaUmLeilao() {
		Leilao leilao = new LeilaoBuilder().comNome("Bicicleta").comValor(2000.0).comDono(usuario).constroi();
		
		usuarioDao.salvar(usuario);
		leilaoDao.salvar(leilao);
		session.flush();
		leilaoDao.deleta(leilao);

		assertNull(leilaoDao.porId(leilao.getId()));
	}

}
