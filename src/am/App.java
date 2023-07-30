package am;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import am.dto.Article;
import am.dto.Member;
import am.util.Util;

public class App {

	private List<Article> articles;
	private List<Member> members;
	private Member loginedMember;

	public App() {
		this.articles = new ArrayList<>();
		this.members = new ArrayList<>();
		this.loginedMember = null;
	}

	public void run() {
		System.out.println("== 프로그램 시작 ==");

		makeTestData();

		Scanner sc = new Scanner(System.in);

		int lastArticleId = 3;
		int lastMemberId = 3;

		while (true) {

			System.out.printf("명령어) ");
			String cmd = sc.nextLine().trim();

			if (cmd.length() == 0) {
				System.out.println("명령어를 입력해주세요");
				continue;
			}

			if (cmd.equals("exit")) {
				break;
			}

			if (cmd.equals("member join")) {

				if (isLogined()) {
					System.out.println("로그아웃 후 이용해주세요");
					continue;
				}

				lastMemberId++;
				String regDate = Util.getDateStr();

				String loginId = null;

				while (true) {
					System.out.printf("로그인 아이디 : ");
					loginId = sc.nextLine();

					if (isLoginIdDup(loginId)) {
						System.out.printf("%s은(는) 이미 사용중인 아이디입니다\n", loginId);
						continue;
					}

					System.out.printf("%s은(는) 사용가능한 아이디입니다\n", loginId);
					break;
				}

				String loginPw = null;

				while (true) {
					System.out.printf("로그인 비밀번호 : ");
					loginPw = sc.nextLine();
					System.out.printf("로그인 비밀번호 확인 : ");
					String loginPwChk = sc.nextLine();

					if (loginPw.equals(loginPwChk) == false) {
						System.out.println("비밀번호가 일치하지 않습니다");
						continue;
					}

					break;
				}

				System.out.printf("이름 : ");
				String name = sc.nextLine();

				Member member = new Member(lastMemberId, regDate, loginId, loginPw, name);

				members.add(member);

				System.out.printf("%s 회원님의 가입이 완료되었습니다\n", loginId);

			} else if (cmd.equals("member login")) {

				if (isLogined()) {
					System.out.println("로그아웃 후 이용해주세요");
					continue;
				}

				System.out.printf("로그인 아이디 : ");
				String loginId = sc.nextLine();
				System.out.printf("로그인 비밀번호 : ");
				String loginPw = sc.nextLine();

				Member foundMember = getMemberByLoginId(loginId);

				if (foundMember == null) {
					System.out.printf("%s은(는) 존재하지 않는 아이디입니다\n", loginId);
					continue;
				}

				if (foundMember.loginPw.equals(loginPw) == false) {
					System.out.println("비밀번호를 확인해주세요");
					continue;
				}

				this.loginedMember = foundMember;

				System.out.printf("로그인 성공! %s님 환영합니다\n", foundMember.name);

			} else if (cmd.equals("member logout")) {

				if (isLogined() == false) {
					System.out.println("로그인 후 이용해주세요");
					continue;
				}

				this.loginedMember = null;

				System.out.println("로그아웃 성공!");

			} else if (cmd.equals("article write")) {

				if (isLogined() == false) {
					System.out.println("로그인 후 이용해주세요");
					continue;
				}

				lastArticleId++;
				String regDate = Util.getDateStr();

				System.out.printf("제목 : ");
				String title = sc.nextLine();
				System.out.printf("내용 : ");
				String body = sc.nextLine();

				Article article = new Article(lastArticleId, regDate, this.loginedMember.id, title, body);

				articles.add(article);

				System.out.printf("%d번 게시글이 생성되었습니다\n", lastArticleId);

			} else if (cmd.startsWith("article list")) {

				if (articles.size() == 0) {
					System.out.println("게시글이 없습니다");
					continue;
				}

				String searchKeyword = cmd.substring("article list".length()).trim();

				List<Article> forPrintArticles = articles;

				if (searchKeyword.length() > 0) {

					System.out.println("검색어 : " + searchKeyword);

					forPrintArticles = new ArrayList<>();

					for (Article article : articles) {
						if (article.title.contains(searchKeyword)) {
							forPrintArticles.add(article);
						}
					}

					if (forPrintArticles.size() == 0) {
						System.out.println("검색결과가 없습니다");
						continue;
					}
				}
				
				System.out.println("번호	|	제목	|		작성일		|	작성자	|	조회수	");

				for (int i = forPrintArticles.size() - 1; i >= 0; i--) {
					Article article = forPrintArticles.get(i);
					
					Member member = getMemberName(article.memberId);
					
					System.out.printf("%d	|	%s	|	%s	|	%s	|	%d	\n", article.id, article.title,
							article.regDate, member.name, article.hit);
				}

			} else if (cmd.startsWith("article detail ")) {

				String[] cmdBits = cmd.split(" ");
				int id = Integer.parseInt(cmdBits[2]);

				Article foundArticle = getArticleById(id);

				if (foundArticle == null) {
					System.out.printf("%d번 게시글은 존재하지 않습니다\n", id);
					continue;
				}

				foundArticle.increaseHit();

				Member member = getMemberName(foundArticle.memberId);
				
				System.out.println("== 게시글 상세보기 ==");
				System.out.printf("번호 : %d\n", foundArticle.id);
				System.out.printf("작성일 : %s\n", foundArticle.regDate);
				System.out.printf("작성자 : %s\n", member.name);
				System.out.printf("조회수 : %d\n", foundArticle.hit);
				System.out.printf("제목 : %s\n", foundArticle.title);
				System.out.printf("내용 : %s\n", foundArticle.body);

			} else if (cmd.startsWith("article modify ")) {

				if (isLogined() == false) {
					System.out.println("로그인 후 이용해주세요");
					continue;
				}

				String[] cmdBits = cmd.split(" ");
				int id = Integer.parseInt(cmdBits[2]);

				Article foundArticle = getArticleById(id);

				if (foundArticle == null) {
					System.out.printf("%d번 게시글은 존재하지 않습니다\n", id);
					continue;
				}

				if (foundArticle.memberId != this.loginedMember.id) {
					System.out.println("해당 게시글에 대한 권한이 없습니다");
					continue;
				}
				
				System.out.printf("수정할 제목 : ");
				String title = sc.nextLine();
				System.out.printf("수정할 내용 : ");
				String body = sc.nextLine();

				foundArticle.title = title;
				foundArticle.body = body;

				System.out.printf("%d번 게시글이 수정되었습니다\n", id);

			} else if (cmd.startsWith("article delete ")) {

				if (isLogined() == false) {
					System.out.println("로그인 후 이용해주세요");
					continue;
				}

				String[] cmdBits = cmd.split(" ");
				int id = Integer.parseInt(cmdBits[2]);

//				int foundIndex = -1;
//
//				for (int i = 0; i < articles.size(); i++) {
//					Article article = articles.get(i);
//					if (article.id == id) {
//						foundIndex = i;
//						break;
//					}
//				}
				
				Article foundArticle = getArticleById(id);

				if (foundArticle == null) {
					System.out.printf("%d번 게시글은 존재하지 않습니다\n", id);
					continue;
				}
				
				if (foundArticle.memberId != this.loginedMember.id) {
					System.out.println("해당 게시글에 대한 권한이 없습니다");
					continue;
				}

				articles.remove(foundArticle);

				System.out.printf("%d번 게시글이 삭제되었습니다\n", id);

			} else {
				System.out.println("존재하지 않는 명령어 입니다");
			}
		}

		sc.close();

		System.out.println("== 프로그램 끝 ==");
	}

	private Article getArticleById(int id) {
		
		for (Article article : articles) {
			if (article.id == id) {
				return article;
			}
		}
		
		return null;
	}

	private Member getMemberName(int memberId) {
		
		for (Member member : members) {
			if (member.id == memberId) {
				return member;
			}
		}
		
		return null;
	}

	private boolean isLogined() {
		return this.loginedMember != null;
	}

	private Member getMemberByLoginId(String loginId) {

		for (Member member : members) {
			if (member.loginId.equals(loginId)) {
				return member;
			}
		}

		return null;
	}

	private boolean isLoginIdDup(String loginId) {

		for (Member member : members) {
			if (member.loginId.equals(loginId)) {
				return true;
			}
		}

		return false;
	}

	private void makeTestData() {
		System.out.println("테스트를 위한 데이터를 생성합니다");

		articles.add(new Article(1, Util.getDateStr(), 2, "제목1", "내용1", 10));
		articles.add(new Article(2, Util.getDateStr(), 2, "제목2", "내용2", 20));
		articles.add(new Article(3, Util.getDateStr(), 1, "제목3", "내용3", 30));

		members.add(new Member(1, Util.getDateStr(), "test1", "test1", "유저1"));
		members.add(new Member(2, Util.getDateStr(), "test2", "test2", "유저2"));
		members.add(new Member(3, Util.getDateStr(), "test3", "test3", "유저3"));
	}
}
