// Po załadowaniu dokumentu wywołujemy funkcję inicjującą
document.addEventListener("DOMContentLoaded", function() {
  const quizContainer = document.getElementById("quiz-container");
  const submitBtn = document.getElementById("submit-btn");
  const resultDiv = document.getElementById("result");

  // 1. Pobierz pytania z backendu
  fetch("/api/questions")
    .then(response => response.json())
    .then(data => {
      // data powinno być tablicą pytań (obiekty { question, options })
      data.forEach((q, index) => {
        // Tworzymy elementy HTML dla pytania
        const questionDiv = document.createElement("div");
        questionDiv.className = "question-block";
        const questionTitle = document.createElement("h3");
        questionTitle.innerText = (index+1) + ". " + q.question;
        questionDiv.appendChild(questionTitle);

        // Dla każdej opcji tworzymy etykietę z input (radio)
        q.options.forEach((option, optIndex) => {
          const optionLabel = document.createElement("label");
          optionLabel.className = "option-label";
          const optionInput = document.createElement("input");
          optionInput.type = "radio";
          optionInput.name = "question" + index;  // grupujemy opcje po nazwie
          optionInput.value = option;
          // id i for nie są konieczne przy dynamicznym tworzeniu, label zawiera input jako child
          optionLabel.appendChild(optionInput);
          optionLabel.appendChild(document.createTextNode(option));
          questionDiv.appendChild(optionLabel);
        });

        quizContainer.appendChild(questionDiv);
      });
      // Po wstawieniu pytań, pokaż przycisk
      submitBtn.classList.remove("hidden");
    })
    .catch(error => {
      quizContainer.innerHTML = "<p class='error'>Nie udało się załadować pytań. Spróbuj odświeżyć stronę.</p>";
      console.error("Błąd podczas pobierania pytań:", error);
    });

  // 2. Po kliknięciu przycisku "Pokaż rekomendację"
  submitBtn.addEventListener("click", function() {
    // Zbierz odpowiedzi zaznaczone przez użytkownika
    const answers = [];
    const questionBlocks = document.querySelectorAll(".question-block");
    let allAnswered = true;
    questionBlocks.forEach((block, qIndex) => {
      const selectedOption = block.querySelector(`input[name=question${qIndex}]:checked`);
      if (selectedOption) {
        answers.push(selectedOption.value);
      } else {
        allAnswered = false;
      }
    });

    if (!allAnswered) {
      alert("Proszę odpowiedzieć na wszystkie pytania przed wysłaniem quizu.");
      return;
    }

    // Wyślij odpowiedzi do backendu
    fetch("/api/recommendation", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ answers: answers })
    })
      .then(response => response.json())
      .then(data => {
        // Oczekujemy, że serwer zwróci JSON { recommendation: "tekst" }
        if (data.recommendation) {
          resultDiv.innerHTML = `<h2>Twoja rekomendacja:</h2><p>${data.recommendation}</p>`;
        } else {
          resultDiv.innerHTML = "<p class='error'>Brak rekomendacji. Spróbuj ponownie później.</p>";
        }
      })
      .catch(error => {
        resultDiv.innerHTML = "<p class='error'>Wystąpił błąd podczas uzyskiwania rekomendacji.</p>";
        console.error("Błąd API rekomendacji:", error);
      });
  });
});
