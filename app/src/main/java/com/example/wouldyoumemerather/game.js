const express = require("express");
const fetch = require("node-fetch");
const app = express();

async function getMemesBR(afterToken = null) {
  try {
    let url = `https://www.reddit.com/r/MemesBR/hot.json?limit=100`;
    if (afterToken) url += `&after=${afterToken}`;

    const res = await fetch(url, {
      headers: { "User-Agent": "nodejs-game/1.0" },
    });

    const json = await res.json();
    const posts = json.data.children;
    const after = json.data.after;

    const memes = posts
      .map((child) => child.data)
      .filter((post) => {
        if (post.over_18) return false;
        if (post.is_video) return false;
        if (!post.url || !post.url.match(/\.(jpg|jpeg|png|gif)$/i))
          return false;
        return true;
      })
      .map((post) => ({
        id: `reddit-${post.id}`,
        titulo: post.title,
        url: post.url,
      }));

    return { memes, after };
  } catch (err) {
    console.error("Erro ao buscar memes:", err);
    return { memes: [], after: null };
  }
}

// guarda os memes em memória
let memesCache = [];
let afterToken = null;

async function carregarMemes() {
  const result = await getMemesBR(afterToken);
  memesCache = [...memesCache, ...result.memes];
  afterToken = result.after;
  console.log(`✅ ${memesCache.length} memes carregados`);
}

// rota que o Java vai chamar
app.get("/pergunta", async (req, res) => {
  try {
    // se tiver poucos memes, carrega mais
    if (memesCache.length < 10) {
      await carregarMemes();
    }

    if (memesCache.length < 2) {
      return res.status(500).json({ erro: "Memes insuficientes" });
    }

    // pega 2 memes aleatórios diferentes
    const index1 = Math.floor(Math.random() * memesCache.length);
    let index2;
    do {
      index2 = Math.floor(Math.random() * memesCache.length);
    } while (index2 === index1);

    const meme1 = memesCache[index1];
    const meme2 = memesCache[index2];

    res.json({
      opcao1: {
        titulo: meme1.titulo,
        url: meme1.url,
      },
      opcao2: {
        titulo: meme2.titulo,
        url: meme2.url,
      },
    });
  } catch (err) {
    res.status(500).json({ erro: "Erro ao buscar memes" });
  }
});

// carrega memes ao iniciar
carregarMemes().then(() => {
  app.listen(3000, () =>
    console.log("✅ Servidor rodando em http://localhost:3000"),
  );
});
