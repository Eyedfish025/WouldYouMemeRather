package com.example.wouldyoumemerather;

public class Meme {
    private String id;
    private String name;
    private String url;

    /**
     * Construtor da classe Meme.
     * @param id Identificador único do meme.
     * @param name Nome ou título do meme.
     * @param url Link da imagem do meme.
     */
    public Meme(String id, String name, String url) {
        this.id = id;
        this.name = name;
        this.url = url;
    }

    /** @return Retorna o ID do meme. */
    public String getId() { return id; }

    /** @return Retorna o nome do meme. */
    public String getName() { return name; }

    /** @return Retorna a URL da imagem do meme. */
    public String getUrl() { return url; }
}
